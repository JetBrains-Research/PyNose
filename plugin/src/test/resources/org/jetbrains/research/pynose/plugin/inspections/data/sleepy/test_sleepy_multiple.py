import unittest
import time


class SomeClass(unittest.TestCase):

    def test_something(self):
        <warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</warning>

    def do_something(self):
        time.sleep(5)

    def test_something_else(self):
        <warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</warning>


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        <warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</warning>


class AnotherClass:

    def test_something_another(self):
        time.sleep(5)
