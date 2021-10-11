class MyTestCase(unittest.TestCase):
    def test_add1(self):
        self.assertEquals(add(4, 5), 9)