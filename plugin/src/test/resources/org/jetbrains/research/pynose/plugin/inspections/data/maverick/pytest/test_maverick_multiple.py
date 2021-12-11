class <warning descr="This test suite fixture's setup method is not used in some of the test cases">TestClass</warning>():
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
        print("hello!")


class <warning descr="This test suite fixture's setup method is not used in some of the test cases">TestOtherClass</warning>():
    x: int
    s: str
    z: str
    w: int = 10

    def setUp(self):
        self.x = 10
        self.s = "hello"
        self.z = "bye"

    def test_something(self):
        print(self.w)

    def test_something_else(self):
        assert self.s != "hi"
        assert self.x != 12
