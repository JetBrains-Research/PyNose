import unittest


class <weak_warning descr="Test suite fixture is too general">SomeClass</weak_warning>(unittest.TestCase):
    x: int
    s: str
    z: str
    w: int = 5

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"
        cls.z = "bye"

    def test_something(self):
        assert self.x == 10
        self.assertNotEqual(self.x, self.w)

    def test_something_else(self):
        assert self.s != "bye"
        assert self.x != 15


class <weak_warning descr="Test suite fixture is too general">OtherClass</weak_warning>(unittest.TestCase):
    x: int
    s: str
    z: str
    w: int = 10

    def setUp(self):
        self.x = 10
        self.s = "hello"
        self.z = "bye"

    def test_something(self):
        assert self.x == 10
        self.assertEqual(self.x, self.w)

    def test_something_else(self):
        assert self.s != "hi"
        assert self.x != 12
