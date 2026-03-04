from unittest import TestCase
from framework import AssemblyTest, print_coverage


class TestAbs(TestCase):
    def test_zero(self):
        t = AssemblyTest(self, "abs.s")
        # load 0 into register a0
        t.input_scalar("a0", 0)
        # call the abs function
        t.call("abs")
        # check that after calling abs, a0 is equal to 0 (abs(0) = 0)
        t.check_scalar("a0", 0)
        # generate the `assembly/TestAbs_test_zero.s` file and run it through venus
        t.execute()

    def test_one(self):
        # same as test_zero, but with input 1
        t = AssemblyTest(self, "abs.s")
        t.input_scalar("a0", 1)
        t.call("abs")
        t.check_scalar("a0", 1)
        t.execute()
    def test_minus_one(self):
        t = AssemblyTest(self, "abs.s")
        t.input_scalar("a0", -1)
        t.call("abs")
        t.check_scalar("a0", 1)
        t.execute()

    @classmethod
    def tearDownClass(cls):
        print_coverage("abs.s", verbose=False)

class TestRelu(TestCase):
    def test_simple(self):
        t = AssemblyTest(self, "relu.s")
        # create an array in the data section
        array0 = t.array([1, -2, 3, -4, 5, -6, 7, -8, 9])
        # load address of `array0` into register a0
        t.input_array("a0", array0)
        # set a1 to the length of our array
        t.input_scalar("a1", len(array0))
        # call the relu function
        t.call("relu")
        # check that the array0 was changed appropriately
        t.check_array(array0, [1, 0, 3, 0, 5, 0, 7, 0, 9])
        # generate the `assembly/TestRelu_test_simple.s` file and run it through venus
        t.execute()
    def test_one(self):
        t = AssemblyTest(self, "relu.s")
        array0 = t.array([-1, -2, 3, -4, 0, -6, 7, -8, -9])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("relu")
        t.check_array(array0, [0, 0, 3, 0, 0, 0, 7, 0, 0])
        t.execute()
    def test_two(self):
        t = AssemblyTest(self, "relu.s")
        array0 = t.array([1, 2, 3, 4, 5, 6, 7, 8])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("relu")
        t.check_array(array0, [1, 2, 3, 4, 5, 6, 7, 8])
        t.execute()
    def test_three(self):
        t = AssemblyTest(self, "relu.s")
        array0 = t.array([-1, -2, -3, -4, -5, 0, 0, -1])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("relu")
        t.check_array(array0, [0, 0, 0, 0, 0, 0, 0, 0])
        t.execute()
    # def test_empty(self):
    #     t = AssemblyTest(self, "relu.s")
    #     array0 = t.array([])
    #     t.input_array("a0", array0)
    #     t.input_scalar("a1", len(array0))
    #     t.call("relu")
    #     t.check_array(array0, [])
    #     t.execute()

    @classmethod
    def tearDownClass(cls):
        print_coverage("relu.s", verbose=False)


class TestArgmax(TestCase):
    def test_simple(self):
        t = AssemblyTest(self, "argmax.s")
        # raise NotImplementedError("TODO")

        # create an array in the data section
        array0 = t.array([1, 2, 3, 1, 5, 3, 5])
        # load address of the array into register a0
        t.input_array("a0", array0)
        # set a1 to the length of the array
        t.input_scalar("a1", len(array0))
        # call the `argmax` function
        t.call("argmax")
        # check that the register a0 contains the correct output
        t.check_scalar("a0", 4)
        # generate the `assembly/TestArgmax_test_simple.s` file and run it through venus
        t.execute()
    def test_one(self):
        t = AssemblyTest(self, "argmax.s")
        array0 = t.array([1, 1, 1, 1, 1])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("argmax")
        t.check_scalar("a0", 0)
        t.execute()
    def test_two(self):
        t = AssemblyTest(self, "argmax.s")
        array0 = t.array([-3, -2, 0, 1, 3, 5])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("argmax")
        t.check_scalar("a0", 5)
        t.execute()
    def test_three(self):
        t = AssemblyTest(self, "argmax.s")
        array0 = t.array([-1, -1, 2, 2, 3, 3, 3, 5, 2, 4, 1, 2, 0])
        t.input_array("a0", array0)
        t.input_scalar("a1", len(array0))
        t.call("argmax")
        t.check_scalar("a0", 7)
        t.execute()
        

    @classmethod
    def tearDownClass(cls):
        print_coverage("argmax.s", verbose=False)


class TestDot(TestCase):
    def test_simple(self):
        t = AssemblyTest(self, "dot.s")
        # raise NotImplementedError("TODO")

        # create arrays in the data section
        array0 = t.array([1, 2, 3, 4, 5])
        array1 = t.array([5, 4, 3, 2, 1])
        # load array addresses into argument registers
        t.input_array("a0", array0)
        t.input_array("a1", array1)
        # load array attributes into argument registers
        t.input_scalar("a2", len(array0))
        t.input_scalar("a3", 1)
        t.input_scalar("a4", 1)
        # call the `dot` function
        t.call("dot")
        # check the return value
        t.check_scalar("a0", 35)
        t.execute()
    def test_one(self):
        t = AssemblyTest(self, "dot.s")
        # raise NotImplementedError("TODO")
        array0 = t.array([1, 2, 3, 4, 5])
        array1 = t.array([1, 2, 3, 4, 5])
        t.input_array("a0", array0)
        t.input_array("a1", array1)
        t.input_scalar("a2", 3)
        t.input_scalar("a3", 2)
        t.input_scalar("a4", 2)
        t.call("dot")
        t.check_scalar("a0", 35)
        t.execute()
    def test_two(self):
        t = AssemblyTest(self, "dot.s")
        # raise NotImplementedError("TODO")
        array0 = t.array([1, 4, 2, 5, 6, 2, 4, 5, 1, 10])
        array1 = t.array([3, 6, 4, 6, 9, 10, 2, 3, 1, 6])
        t.input_array("a0", array0)
        t.input_array("a1", array1)
        t.input_scalar("a2", 5)
        t.input_scalar("a3", 1)
        t.input_scalar("a4", 2)
        t.call("dot")
        t.check_scalar("a0", 53)
        t.execute()
    def test_three(self):
        t = AssemblyTest(self, "dot.s")
        # raise NotImplementedError("TODO")
        array0 = t.array([1, 4, 2, 5, 6, 2, 4, 5, 1, 10])
        array1 = t.array([3, 6, 4, 6, 9, 10, 2, 3, 1, 6])
        t.input_array("a0", array0)
        t.input_array("a1", array1)
        t.input_scalar("a2", 4)
        t.input_scalar("a3", 3)
        t.input_scalar("a4", 2)
        t.call("dot")
        t.check_scalar("a0", 79)
        t.execute()

    @classmethod
    def tearDownClass(cls):
        print_coverage("dot.s", verbose=False)


class TestMatmul(TestCase):

    def do_matmul(self, m0, m0_rows, m0_cols, m1, m1_rows, m1_cols, result, code=0):
        t = AssemblyTest(self, "matmul.s")
        # we need to include (aka import) the dot.s file since it is used by matmul.s
        t.include("dot.s")

        # create arrays for the arguments and to store the result
        array0 = t.array(m0)
        array1 = t.array(m1)
        array_out = t.array([0] * len(result))

        # raise NotImplementedError("TODO")

        # load address of input matrices and set their dimensions
        t.input_array("a0", array0)
        t.input_scalar("a1", m0_rows)
        t.input_scalar("a2", m0_cols)
        t.input_array("a3", array1)
        t.input_scalar("a4", m1_rows)
        t.input_scalar("a5", m1_cols)
        # load address of output array
        t.input_array("a6", array_out)
        # call the matmul function
        t.call("matmul")

        # check the content of the output array
        t.check_array(array_out, result)

        # generate the assembly file and run it through venus, we expect the simulation to exit with code `code`
        t.execute(code=code)

    def test_simple(self):
        self.do_matmul(
            [1, 2, 3, 4, 5, 6, 7, 8, 9], 3, 3,
            [1, 2, 3, 4, 5, 6, 7, 8, 9], 3, 3,
            [30, 36, 42, 66, 81, 96, 102, 126, 150]
        )
    def test_one(self):
        self.do_matmul(
            [0, 0, 0, 0, 0, 0, 0, 0, 0], 3, 3,
            [1, 2, 3, 4, 5, 6, 7, 8, 9], 3, 3,
            [0, 0, 0, 0, 0, 0, 0, 0, 0]
        )
    def test_two(self):
        self.do_matmul(
            [4, 2, 6, 2, 6, 3], 2, 3,
            [7, 3, 1, 3, 6, 2], 3, 2,
            [66, 30, 38, 30]
        )
    def test_not_match(self):
        self.do_matmul(
            [1, 1, 1, 1, 1, 1], 2, 3,
            [1, 1, 1, 1], 2, 2,
            [0, 0, 0, 0]
        )

    @classmethod
    def tearDownClass(cls):
        print_coverage("matmul.s", verbose=False)


class TestReadMatrix(TestCase):

    def do_read_matrix(self, fail='', code=0):
        t = AssemblyTest(self, "read_matrix.s")
        # load address to the name of the input file into register a0
        # t.input_read_filename("a0", "inputs/test_read_matrix/test_input.bin")
        t.input_read_filename("a0", "inputs/simple2/bin/m0.bin")
        # allocate space to hold the rows and cols output parameters
        rows = t.array([-1])
        cols = t.array([-1])

        # load the addresses to the output parameters into the argument registers
        t.input_array("a1", rows)
        t.input_array("a2", cols)

        # call the read_matrix function
        t.call("read_matrix")

        # check the output from the function
        t.check_array_pointer("a0", [11, -10, 13, 10, -23, -6, -22, 10])
        # generate assembly and run it through venus
        t.execute(fail=fail, code=code)

    def test_simple(self):
        self.do_read_matrix()

    @classmethod
    def tearDownClass(cls):
        print_coverage("read_matrix.s", verbose=False)


class TestWriteMatrix(TestCase):

    def do_write_matrix(self, fail='', code=0, outfile='', sourcefile='', row=0, col=0, arr=[]):
        t = AssemblyTest(self, "write_matrix.s")
        # load output file name into a0 register
        t.input_write_filename("a0", outfile)
        # load input array and other arguments
        result = t.array(arr)
        t.input_scalar("a2", row)
        t.input_scalar("a3", col)
        t.input_array("a1", result)
        # call `write_matrix` function
        t.call("write_matrix")
        # generate assembly and run it through venus
        t.execute(fail=fail, code=code)
        # compare the output file against the reference
        t.check_file_output(outfile, sourcefile)

    def test_simple(self):
        outfile = "outputs/test_write_matrix/student.bin"
        sourcefile = "outputs/test_write_matrix/reference.bin"
        row, col = 3, 3
        arr = [1, 2, 3, 4, 5, 6, 7, 8, 9]
        self.do_write_matrix(outfile=outfile, sourcefile=sourcefile, row=row, col=col, arr=arr)

    def test_one(self):
        outfile = "outputs/test_write_matrix/student.bin"
        sourcefile = "outputs/test_write_matrix/test1.bin"
        row, col = 3, 6
        arr = [1, 3, 5, 7, 2, 4, 12, 43, -12, 332, -2, -1, 0, 0, 1, 22, 23, -12]
        self.do_write_matrix(outfile=outfile, sourcefile=sourcefile, row=row, col=col, arr=arr)

    def test_two(self):
        outfile = "outputs/test_write_matrix/student.bin"
        sourcefile = "outputs/test_write_matrix/test2.bin"
        row, col = 6, 3
        arr = [1, 3, 5, 7, 2, 4, 12, 43, -12, 332, -2, -1, 0, 0, 1, 22, 23, -12]
        self.do_write_matrix(outfile=outfile, sourcefile=sourcefile, row=row, col=col, arr=arr)

    @classmethod
    def tearDownClass(cls):
        print_coverage("write_matrix.s", verbose=False)


class TestClassify(TestCase):

    def make_test(self):
        t = AssemblyTest(self, "classify.s")
        t.include("argmax.s")
        t.include("dot.s")
        t.include("matmul.s")
        t.include("read_matrix.s")
        t.include("relu.s")
        t.include("write_matrix.s")
        return t

    def test_simple0_input0(self):
        t = self.make_test()
        out_file = "outputs/test_basic_main/student0.bin"
        ref_file = "outputs/test_basic_main/reference0.bin"
        args = ["inputs/simple0/bin/m0.bin", "inputs/simple0/bin/m1.bin",
                "inputs/simple0/bin/inputs/input0.bin", out_file]
        # call classify function
        t.call("classify")
        # generate assembly and pass program arguments directly to venus
        t.execute(args=args)

        # compare the output file and
        raise NotImplementedError("TODO")
        # TODO
        # compare the classification output with `check_stdout`

    @classmethod
    def tearDownClass(cls):
        print_coverage("classify.s", verbose=False)


class TestMain(TestCase):

    def run_main(self, inputs, output_id, label):
        args = [f"{inputs}/m0.bin", f"{inputs}/m1.bin", f"{inputs}/inputs/input0.bin",
                f"outputs/test_basic_main/student{output_id}.bin"]
        reference = f"outputs/test_basic_main/reference{output_id}.bin"
        t = AssemblyTest(self, "main.s", no_utils=True)
        t.call("main")
        t.execute(args=args, verbose=False)
        t.check_stdout(label)
        t.check_file_output(args[-1], reference)

    def run_mnist(self, inputs, output_id, label, id):
        args = [f"{inputs}/m0.bin", f"{inputs}/m1.bin", f"{inputs}/inputs/mnist_input{id}.bin",
                f"outputs/test_basic_main/student{output_id}.bin"]
        # reference = f"outputs/test_basic_main/reference{output_id}.bin"
        t = AssemblyTest(self, "main.s", no_utils=True)
        t.call("main")
        t.execute(args=args, verbose=False)
        t.check_stdout(label)
        # t.check_file_output(args[-1], reference)

    def test0(self):
        self.run_main("inputs/simple0/bin", "0", "2")

    def test1(self):
        self.run_main("inputs/simple1/bin", "1", "1")

    def test_mnist0(self):
        self.run_mnist("inputs/mnist/bin", "2", "6", 0)

    def test_mnist1(self):
        self.run_mnist("inputs/mnist/bin", "3", "9", 1)

    def test_mnist2(self):
        self.run_mnist("inputs/mnist/bin", "4", "7", 2)

    def test_mnist3(self):
        self.run_mnist("inputs/mnist/bin", "5", "2", 3)

    def test_mnist4(self):
        self.run_mnist("inputs/mnist/bin", "6", "9", 4)

    def test_mnist5(self):
        self.run_mnist("inputs/mnist/bin", "7", "4", 5)

    def test_mnist6(self):
        self.run_mnist("inputs/mnist/bin", "8", "4", 6)

    def test_mnist7(self):
        self.run_mnist("inputs/mnist/bin", "9", "2", 7)

    def test_mnist8(self):
        self.run_mnist("inputs/mnist/bin", "10", "7", 8)
