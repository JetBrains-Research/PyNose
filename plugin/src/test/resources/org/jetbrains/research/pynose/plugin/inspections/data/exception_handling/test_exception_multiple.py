import unittest

class SomeClass(unittest.TestCase):
    def test_something(self):
        <warning descr="Consider utilizing functionality of your testing framework to handle the exception">try</warning>:
            x = 3
        except ValueError:
            print("Try again...")

    def do_something(self):
        raise NameError('HiThere')

    def test_something_else(self):
        <warning descr="Consider utilizing functionality of your testing framework to handle the exception">raise</warning> NameError('HiThere')

class OtherClass(unittest.TestCase):
    def test_something_other(self):
        <warning descr="Consider utilizing functionality of your testing framework to handle the exception">try</warning>:
            x = 5
        except ValueError:
            print("Fail!")

class AnotherClass():
    def test_something_another(self):
        raise NameError('HiThere')