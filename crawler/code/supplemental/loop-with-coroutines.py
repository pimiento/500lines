#!/usr/bin/env python3.5

"""Sloppy little crawler, demonstrates a hand-made event loop and coroutines.

First read loop-with-callbacks.py. This example builds on that one, replacing
callbacks with generators.
"""
import re
import time
import socket
import logging
import urllib.parse
from selectors import DefaultSelector, EVENT_READ, EVENT_WRITE

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("COROUTINES")


class Future:
    def __init__(self):
        self.result = None
        self._callbacks = []

    def result(self):
        return self.result

    def add_done_callback(self, fn):
        self._callbacks.append(fn)

    def set_result(self, result):
        self.result = result
        for fn in self._callbacks:
            fn(self)

    def __iter__(self):
        yield self  # This tells Task to wait for completion.
        return self.result


class Task:
    def __init__(self, coro):
        logger.info("coro is %r", coro)
        self.coro = coro
        f = Future()
        logger.info("new Future in Task is %r", f)
        f.set_result(None)
        self.step(f)

    def step(self, future):
        logger.info("step %r", future)
        try:
            next_future = self.coro.send(future.result)
            logger.info("next future is %r", next_future)
        except StopIteration:
            logger.info("the end of Task %r", self)
            return

        next_future.add_done_callback(self.step)


urls_seen = set(['/'])
urls_todo = set(['/'])
concurrency_achieved = 0
selector = DefaultSelector()
stopped = False


def connect(sock, address):
    f = Future()
    logger.info("new Future in connect is %r", f)
    sock.setblocking(False)
    try:
        sock.connect(address)
    except BlockingIOError:
        pass

    def on_connected():
        f.set_result(None)

    selector.register(sock.fileno(), EVENT_WRITE, on_connected)
    yield from f
    selector.unregister(sock.fileno())


def read(sock):
    f = Future()
    logger.info("new Future in read is %r", f)

    def on_readable():
        f.set_result(sock.recv(4096))  # Read 4k at a time.

    selector.register(sock.fileno(), EVENT_READ, on_readable)
    logger.info("f is %r", f)
    chunk = yield from f
    logger.info("chunk is %r", chunk)
    selector.unregister(sock.fileno())
    return chunk


def read_all(sock):
    response = []
    chunk = yield from read(sock)
    while chunk:
        logger.info("yielded chunk is %r", chunk)
        response.append(chunk)
        chunk = yield from read(sock)
    logger.info("response is %r", response)
    return b''.join(response)


class Fetcher:
    def __init__(self, url):
        self.response = b''
        self.url = url

    def fetch(self):
        global concurrency_achieved, stopped
        concurrency_achieved = max(concurrency_achieved, len(urls_todo))

        sock = socket.socket()
        yield from connect(sock, ('xkcd.com', 80))
        get = 'GET {} HTTP/1.0\r\nHost: xkcd.com\r\n\r\n'.format(self.url)
        sock.send(get.encode('ascii'))
        self.response = yield from read_all(sock)

        self._process_response()
        urls_todo.remove(self.url)
        if not urls_todo:
            stopped = True
        print(self.url)

    def body(self):
        body = self.response.split(b'\r\n\r\n', 1)[1]
        return body.decode('utf-8')

    def _process_response(self):
        if not self.response:
            print('error: {}'.format(self.url))
            return
        if not self._is_html():
            return
        urls = set(re.findall(r'''(?i)href=["']?([^\s"'<>]+)''',
                              self.body()))

        for url in urls:
            normalized = urllib.parse.urljoin(self.url, url)
            parts = urllib.parse.urlparse(normalized)
            if parts.scheme not in ('', 'http', 'https'):
                continue
            host, port = urllib.parse.splitport(parts.netloc)
            if host and host.lower() not in ('xkcd.com', 'www.xkcd.com'):
                continue
            defragmented, frag = urllib.parse.urldefrag(parts.path)
            if defragmented not in urls_seen:
                urls_todo.add(defragmented)
                urls_seen.add(defragmented)
                Task(Fetcher(defragmented).fetch())

    def _is_html(self):
        head, body = self.response.split(b'\r\n\r\n', 1)
        headers = dict(h.split(': ') for h in head.decode().split('\r\n')[1:])
        return headers.get('Content-Type', '').startswith('text/html')


start = time.time()
fetcher = Fetcher('/')
Task(fetcher.fetch())

while not stopped:
    logger.info("cycle step")
    events = selector.select()
    for event_key, event_mask in events:
        callback = event_key.data
        logger.info("callback is %r", callback)
        callback()

print('{} URLs fetched in {:.1f} seconds, achieved concurrency = {}'.format(
    len(urls_seen), time.time() - start, concurrency_achieved))
