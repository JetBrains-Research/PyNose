import unittest


class SomeClass(unittest.TestCase):

    def __init__(self, *args, **kwargs):
        super(TestingClass, self).__init__(*args, **kwargs)

    def test_something(self):
        pass


class OtherClass(unittest.TestCase):

    def __init__(self, *args, **kwargs):
        super(TestingClass, self).__init__(*args, **kwargs)

    def test_something_other(self):
        pass


class AnotherClass:

    def __init__(self):
        pass

    def test_something_else(self):
        pass
