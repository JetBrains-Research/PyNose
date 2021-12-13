import unittest
import time


class SomeClass(unittest.TestCase):

    def test_something(self):
        time.sleep(0.1)

    def do_something(self):
        time.sleep(0.1)

    def test_something_else(self):
        time.sleep(0.4)


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        time.sleep(0.1)
        time.sleep(0.2)
        time.sleep(0.3)


class AnotherClass:

    def test_something_another(self):
        time.sleep(0.1)
