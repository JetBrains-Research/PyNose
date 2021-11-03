import unittest

class SomeClass(unittest.TestCase):

    def <warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_add2</warning>(self):
        self.assertEqual(add(4, 5), 9)
        self.assertEqual(add(4, 5), 9)
        self.assertEqual(add(4, 5), 4)
        self.assertEqual(add(4, 5), 6)

    def <warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_roulette_1</warning>(self):
        assert 2 == 2
        self.assertEqual(add(4, 5), 9)
        assert "H" == "J"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False


class SomeOtherClass(unittest.TestCase):

    def <warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_roulette_2</warning>(self):
        self.assertEqual(add(4, 5), 9)
        self.assertEqual(add(4, 5), 3)
        self.assertEqual(add(4, 5), 4)
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        assert "H" == "J"


class OtherClass():

    def test_roulette(self):
        assert True
        assert 2 == 2
        assert "H" != "J"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False
