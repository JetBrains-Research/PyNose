import unittest


class SomeClass(unittest.TestCase):

    def test_add2(self):
        self.assertEqual(4 + 5, 9)
        self.assertEqual(4 + 5, 9)
        self.assertNotEqual(4 + 5, 4)
        self.assertNotEqual(4 + 5, 5)

    def test_roulette_1(self):
        assert 2 == 2, "comment"
        self.assertEqual(4 + 5, 9, msg="f")
        # assert "H" == "J", "comment"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False


class SomeOtherClass(unittest.TestCase):

    def test_roulette_2(self):
        self.assertEqual(4 + 5, 9)
        self.assertNotEqual(4 + 5, 4)
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        assert "H" == "J"


class OtherClass:

    def test_roulette(self):
        assert True
        assert 2 == 2
        assert "H" != "J"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False
