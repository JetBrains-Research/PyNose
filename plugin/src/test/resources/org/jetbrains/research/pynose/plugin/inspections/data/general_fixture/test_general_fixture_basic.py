import unittest


class <warning descr="Test suite fixture is too general">SomeClass</warning>(unittest.TestCase):

    x: int
    s: str

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"

    def test_something(self):
        assert self.x == 10
        self.x -= 1
        assert self.x == 9
