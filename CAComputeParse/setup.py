from distutils.core import setup
from Cython.Build import cythonize

setup(name='CACompute',
      ext_modules=cythonize("CACompute.pyx",
                            annotate=True,
                            include_path=["../RuleParser/RuleParser.pyx"],
                            compiler_directives={'language_level' : 3,
                                                 'cdivision' : True}))