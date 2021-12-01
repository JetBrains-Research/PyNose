import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        <warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 2 == 2</warning>

    def do_something(self):
        self.assertEqual(1, 1)

    def test_something_else(self):
        <warning descr="Consider replacing numeric literals with more descriptive constants or variables">self.assertEqual(1, 1)</warning>


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        <warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 5 != 6</warning>


class AnotherClass:

    def test_something_another(self):
        assert 5 == 5
