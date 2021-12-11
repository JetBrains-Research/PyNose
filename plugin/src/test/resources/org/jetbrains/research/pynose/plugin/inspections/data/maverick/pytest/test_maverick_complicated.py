class <warning descr="This test suite fixture's setup method is not used in some of the test cases">TestClass</warning>():
    x : int
    s : str
    z : str
    w = 2

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"
        cls.z = "bye"

    def test_something(self):
        print(self.s + ", world!")
        print(self.z)

    def test_something_else(self):
        assert self.w != 1
