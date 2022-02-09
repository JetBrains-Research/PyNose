class TestClass:
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
        assert self.x != self.w

    def <weak_warning descr="Test suite fixture's setup method is not used in this test case">test_something_else</weak_warning>(self):
        print("hello!")


class TestOtherClass:
    x: int
    s: str
    z: str
    w: int = 10

    def setUp(self):
        self.x = 10
        self.s = "hello"
        self.z = "bye"

    def <weak_warning descr="Test suite fixture's setup method is not used in this test case">test_something</weak_warning>(self):
        print(self.w)

    def test_something_else(self):
        assert self.s != "hi"
        assert self.x != 12
