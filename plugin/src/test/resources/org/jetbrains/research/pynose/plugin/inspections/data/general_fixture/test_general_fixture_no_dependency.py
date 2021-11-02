import unittest


class SomeClass():
    x: int
    s: str
    z: str
    w: int = 5

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"
        cls.z = "bye"

    def test_something(self):
        assert self.x == 10
        assert self.x != self.w

    def test_something_else(self):
        assert self.s != "bye"
        assert self.x != 15
