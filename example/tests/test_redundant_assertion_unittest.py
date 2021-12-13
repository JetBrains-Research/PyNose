import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        assert 1 == 1
        self.assertTrue(1)
        self.assertEqual(2 + 1, 3)
        self.assertTrue(22)
        self.assertEqual(1, 1)

    def do_something(self):
        assert 2 <= 2

    def test_something_else(self):
        assert "a" <= "a"


class OtherClass(unittest.TestCase):

    def test_somethin_other(self):
        assert 1 == 1

    def test_something_other(self):
        self.assertTrue(4 >= 4)


class AnotherClass:

    def test_something_another(self):
        assert 5 == 5
