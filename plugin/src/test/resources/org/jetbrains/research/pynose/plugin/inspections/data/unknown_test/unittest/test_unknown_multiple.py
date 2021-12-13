import unittest


class SomeClass(unittest.TestCase):

    def <weak_warning descr="This test case has no assertion in it">test_something</weak_warning>(self):
        pass

    def do_something(self):
        pass

    def <weak_warning descr="This test case has no assertion in it">test_something_else</weak_warning>(self):
        x = 2


class OtherClass(unittest.TestCase):

    def <weak_warning descr="This test case has no assertion in it">test_something_other</weak_warning>(self):
        print("Hello")


class AnotherClass:

    def test_something_another(self):
        x = 3 + 6
