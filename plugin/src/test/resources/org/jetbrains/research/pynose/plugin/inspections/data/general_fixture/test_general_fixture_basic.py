import unittest


class <weak_warning descr="Test suite fixture is too general">SomeClass</weak_warning>(unittest.TestCase):
    x: int
    s: str

    def setUp(self):
        self.x = 10
        self.s = "hello"

    def test_something(self):
        assert self.x == 10
        self.x -= 1
        assert self.x == 9
