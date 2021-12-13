import unittest


class SomeClass(unittest.TestCase):

    def <weak_warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_something</weak_warning>(self):
        self.assertEqual(4 + 5, 9)
        self.assertEqual(4 + 5, 9)
        self.assertNotEqual(4 + 5, 4)
        self.assertNotEqual(4 + 5, 5)

    def <weak_warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_something_else</weak_warning>(self):
        assert 2 == 2
        self.assertEqual(4 + 5, 9)
        assert "H" == "J"

    def test_roulette_with_comments(self):
        assert 2 == 2, "comment"
        assert 4 + 5 == 9
        assert "H" == "J", "comment"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False


def test_outside(self):
    assert 2 == 2
    self.assertEqual(4 + 5, 9)
    assert "H" == "J"

class SomeOtherClass(unittest.TestCase):

    def <weak_warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_something</weak_warning>(self):
        self.assertEqual(4 + 5, 9)
        self.assertNotEqual(4 + 5, 4)
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        assert "H" == "J"


    def test_roulette_with_comments(self):
        self.assertTrue(2 == 2, "comment")
        self.assertEqual(4 + 5, 9, "comment")
        assert "H" == "J", "comment"


class OtherClass:

    def test_roulette(self):
        assert True
        assert 2 == 2
        assert "H" != "J"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False
