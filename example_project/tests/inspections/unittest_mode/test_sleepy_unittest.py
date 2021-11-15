import unittest
import time


class SomeClass(unittest.TestCase):

    def test_something(self):
        time.sleep(5)

    def do_something(self):
        time.sleep(5)

    def test_something_else(self):
        time.sleep(5)


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        time.sleep(5)


class AnotherClass:

    def test_something_another(self):
        time.sleep(5)
