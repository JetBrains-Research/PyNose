import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        assert 2 == 27

    def do_something(self):
        self.assertEqual(1, 1)

    def test_something_else(self):
        self.assertEqual(1, 31)


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        assert 5 != 6


class AnotherClass:

    def test_something_another(self):
        assert 5 == 5
