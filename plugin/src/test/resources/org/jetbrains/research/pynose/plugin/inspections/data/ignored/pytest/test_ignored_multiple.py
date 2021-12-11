import pytest

class TestClass:
    x: int = 10
    s: str = "hello"
    z: str = "bye"
    w: int = 5

    @pytest.mark.skip()
    def <warning descr="Consider adding the reason why the test is marked as ignored">test_something</warning>(self):
        assert self.x == 10
        assert self.x != self.w

    def test_something_else(self):
        assert self.s != "bye"
        assert self.x != 15

    @pytest.mark.skip(reason="reason")
    def test_something_another(self):
        assert self.s != "bye"
        assert self.x != 15

    @pytest.mark.skip()
    def do_something(self):
        print("did something")

    @pytest.mark.xfail(reason="reason")
    def test_something_another(self):
        assert self.s != "bye"
        assert self.x != 15

    @pytest.mark.xfail()
    def <warning descr="Consider adding the reason why the test is marked as ignored">test_something_another</warning>(self):
        assert self.s != "bye"
        assert self.x != 15

class TestOtherClass():
    x: int
    s: str
    z: str
    w: int = 10

    @pytest.mark.skip()
    def setUp(self):
        self.x = 10
        self.s = "hello"
        self.z = "bye"

    @pytest.mark.skipif(w < 10)
    def <warning descr="Consider adding the reason why the test is marked as ignored">test_something_other</warning>(self):
        assert self.x == 10
        assert self.x == self.w

    def test_something_else_other(self):
        assert self.s != "hi"
        assert self.x != 12

    @pytest.mark.skipUnless(x < 5, reason="reason")
    def test_something_else_another(self):
        assert self.s != "hi"
        assert self.x != 12


@pytest.mark.skip()
class <warning descr="Consider adding the reason why the test is marked as ignored">TestAnotherClass</warning>():
    @pytest.mark.skip()
    def <warning descr="Consider adding the reason why the test is marked as ignored">test_something</warning>(self):
        assert 1 == 1
