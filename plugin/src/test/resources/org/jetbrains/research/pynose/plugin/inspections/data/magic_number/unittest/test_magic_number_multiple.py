import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        <weak_warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 24 == 2</weak_warning>

    def do_something(self):
        self.assertEqual(14, 1)

    def test_something_else(self):
        self.assertEqual(3, 1)


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        <weak_warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 50 != 6</weak_warning>


class AnotherClass:

    def test_something_another(self):
        assert 5 == 54
