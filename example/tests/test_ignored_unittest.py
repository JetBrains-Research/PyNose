import unittest


class SomeClass(unittest.TestCase):
    x: int = 10
    s: str = "hello"
    z: str = "bye"
    w: int = 5

    @unittest.skip("reason")
    def test_something(self):
        assert self.x == 10
        self.assertNotEqual(self.x, self.w)

    @unittest.skip()
    def test_something_else(self):
        assert self.s != "bye"
        assert self.x != 15

    @unittest.skip("reason")
    def do_something(self):
        print("did something")


@unittest.skipIf(2 > 1)
class OtherClass(unittest.TestCase):
    x: int
    s: str
    z: str
    w: int = 10

    @unittest.skip()
    def setUp(self):
        self.x = 10
        self.s = "hello"
        self.z = "bye"

    @unittest.skipIf(w < 10, "reason")
    def test_something_other(self):
        assert self.x == 10
        self.assertEqual(self.x, self.w)

    @unittest.skipIf(w < 10)
    def test_something_else_other(self):
        assert self.s != "hi"
        assert self.x != 12


@unittest.skip("reason")
class AnotherClass:
    @unittest.skip("another reason")
    def test_something(self):
        assert 1 == 1
