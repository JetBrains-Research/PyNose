import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        assert 1 == 1
        self.assertFalse(1 == 2)
        assert 1 == 1

    def test_something_else(self):
        self.assertFalse(1 == 2)
        self.assertFalse(1 == 3)
        assert 1 == 1
        assert 1 == 1

    def not_a_test(self):
        self.assertFalse(1 == 2)
        assert 1 == 1
        assert 1 == 1
        self.assertFalse(1 == 2)


class OtherClass(unittest.TestCase):

    def test_something(self):
        assert 1 == 1
        assert 5 == 5
        assert 5 == 5


def test_something_other():
    assert 1 == 1
    assert 1 == 1


class AnotherClass:

    def test_something_another(self):
        assert 1 == 1
        assert 1 == 1
