import unittest


class SomeClass(unittest.TestCase):
    x: int
    s: str
    w = 2

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"

    def test_something(self):
        print("Hello, world!")
        self.assertEqual(self.s, "hello")

    def test_something_else(self):
        assert self.w != self.x
