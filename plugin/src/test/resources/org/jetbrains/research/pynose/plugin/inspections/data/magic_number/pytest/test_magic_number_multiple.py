import unittest


class TestClass:

    def test_something(self):
        <weak_warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 24 == 2</weak_warning>

    def do_something(self):
        assert 1 == 15

    def test_something_else(self):
        assert 1 == 5



def test_something_other(self):
    <weak_warning descr="Consider replacing numeric literals with more descriptive constants or variables">assert 56 != 6</weak_warning>


class AnotherClass:

    def test_something_another(self):
        assert 5 == 53
