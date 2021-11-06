import unittest


class SomeClass():
    def __init__(self):
        pass

class SomeTest(SomeClass, unittest.TestCase):
    def test_something(self):
        pass
