import unittest


class SomeClass(unittest.TestCase):
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
        assert self.z != self.s

    def test_something_else(self):
        self.assertEqual(self.x, 10)
        assert self.s != "bye"
        assert self.z == "bye"
