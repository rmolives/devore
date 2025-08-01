# Devore Language

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

## About

This is the interpreter of Devore Language, a dialect of Lisp, running on JVM platform.

Once a feature is finished and tested, and not considered harmful, I'll copy the codes here and publish releases.

## Examples

### Sqrt

```lisp
(def (mysqrt x)
    (def (good-enough guess)
        (< (abs (- (pow guess 2) x)) 0.001))
    (def (improve guess)
        (average guess (/ x guess)))
    (def (sqrt-iter guess)
        (if (good-enough guess)
            guess
            (sqrt-iter (improve guess))))
    (sqrt-iter 1.0))
(println (mysqrt 81))
```

### Prime?

```lisp
(def (myprime? n)
    (def (divides? a b)
        (= (mod b a) 0))
    (def (find-divisor n test-divisor)
        (cond ((> (sqrt test-divisor) n) n)
            ((divides? test-divisor n) test-divisor)
            (else (find-divisor n (+ test-divisor 1)))))
    (def (smallest-divisor n)
        (find-divisor n 2))
    (= n (smallest-divisor n)))
(println (myprime? 17))
```

### Fibonacci

```lisp
(def (fib n)
    (cond ((= n 0) 0)
        ((= n 1) 1)
        (else (+ (fib (- n 1)) (fib (- n 2))))))
(println (fib 10))
```
