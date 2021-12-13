import unittest


class SomeClass(unittest.TestCase):
    x: int = 10
    s: str = "hello"
    z: str = "bye"
    w: int = 5

    @unittest.skip()
    def <weak_warning descr="Consider adding the reason why the test is marked as ignored">test_something</weak_warning>(self):
        assert self.x == 10
        self.assertNotEqual(self.x, self.w)

    def test_something_else(self):
        assert self.s != "bye"
        assert self.x != 15

    @unittest.skip("reason")
    def test_something_another(self):
        assert self.s != "bye"
        assert self.x != 15

    @unittest.skip()
    def do_something(self):
        print("did something")


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

    @unittest.skipIf(w < 10)
    def <weak_warning descr="Consider adding the reason why the test is marked as ignored">test_something_other</weak_warning>(self):
        assert self.x == 10
        self.assertEqual(self.x, self.w)

    def test_something_else_other(self):
        assert self.s != "hi"
        assert self.x != 12

    @unittest.skipUnless(x < 5, "reason")
    def test_something_else_another(self):
        assert self.s != "hi"
        assert self.x != 12


@unittest.skip()
class <weak_warning descr="Consider adding the reason why the test is marked as ignored">AnotherSkippedClass</weak_warning>(unittest.TestCase):
    @unittest.skip()
    def <weak_warning descr="Consider adding the reason why the test is marked as ignored">test_something</weak_warning>(self):
        assert 1 == 1


@unittest.skip()
class AnotherClass:
    @unittest.skip()
    def test_something(self):
        assert 1 == 1
