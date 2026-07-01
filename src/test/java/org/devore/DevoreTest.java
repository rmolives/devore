package org.devore;

import org.devore.lang.Env;
import org.devore.lang.token.DToken;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevoreTest {
    private static DToken eval(String code, String... modules) {
        Env env = Devore.newEnv(modules);
        return Devore.call(env, code);
    }

    private static void assertDevoreTrue(String code, String... modules) {
        assertEquals("true", eval(code, modules).toString(), code);
    }

    private static void assertDevoreEquals(String expected, String code, String... modules) {
        assertEquals(expected, eval(code, modules).toString(), code);
    }

    @Nested
    class CoreNumbers {
        @Test void add() { assertDevoreEquals("6", "(+ 1 2 3)"); }
        @Test void subtract() { assertDevoreEquals("6", "(- 10 3 1)"); }
        @Test void negate() { assertDevoreEquals("-7", "(- 7)"); }
        @Test void multiply() { assertDevoreEquals("24", "(* 2 3 4)"); }
        @Test void divide() { assertDevoreEquals("4", "(/ 20 5)"); }
        @Test void average() { assertDevoreEquals("3", "(average 2 3 4)"); }
        @Test void mod() { assertDevoreEquals("1", "(mod 10 3)"); }
        @Test void rem() { assertDevoreEquals("1", "(rem 10 3)"); }
        @Test void abs() { assertDevoreEquals("5", "(abs -5)"); }
    }

    @Nested
    class CoreDefinitions {
        @Test void export() { assertDevoreEquals("export", "(type (export x))"); }
        @Test void undef() { assertDevoreTrue("(begin (def x 1) (undef x) (not (bound? x)))"); }
        @Test void defValue() { assertDevoreEquals("3", "(begin (def x 3) x)"); }
        @Test void defProcedure() { assertDevoreEquals("5", "(begin (def (inc x) (+ x 1)) (inc 4))"); }
        @Test void setValue() { assertDevoreEquals("4", "(begin (def x 1) (set! x 4) x)"); }
        @Test void setProcedure() { assertDevoreEquals("8", "(begin (def (f x) x) (set! (f x) (* x 2)) (f 4))"); }
        @Test void let() { assertDevoreEquals("5", "(let [(x 2) (y 3)] (+ x y))"); }
        @Test void lambda() { assertDevoreEquals("7", "((lambda (x) (+ x 2)) 5)"); }
        @Test void apply() { assertDevoreEquals("6", "(apply + 1 2 3)"); }
        @Test void act() { assertDevoreEquals("6", "(act + (list 1 2 3))"); }
        @Test void defMacro() { assertDevoreEquals("9", "(begin (def-macro (twice x) (+ x x)) (twice 4.5))"); }
        @Test void setMacro() { assertDevoreEquals("7", "(begin (def-macro (m x) x) (set-macro! (m x) (+ x 1)) (m 6))"); }
    }

    @Nested
    class CoreComparisonAndControl {
        @Test void greaterThan() { assertDevoreTrue("(> 3 2)"); }
        @Test void lessThan() { assertDevoreTrue("(< 2 3)"); }
        @Test void equalsProcedure() { assertDevoreTrue("(= 3 3)"); }
        @Test void notEquals() { assertDevoreTrue("(/= 3 4)"); }
        @Test void greaterThanOrEquals() { assertDevoreTrue("(>= 3 3)"); }
        @Test void lessThanOrEquals() { assertDevoreTrue("(<= 3 3)"); }
        @Test void unlessProcedure() { assertDevoreEquals("5", "(unless false 5)"); }
        @Test void whenProcedure() { assertDevoreEquals("5", "(when true 5)"); }
        @Test void ifProcedure() { assertDevoreEquals("1", "(if true 1 2)"); }
        @Test void cond() { assertDevoreEquals("2", "(cond [false 1] [else 2])"); }
        @Test void tryProcedure() { assertDevoreEquals("boom", "(try (error \"boom\") (catch e e))"); }
        @Test void begin() { assertDevoreEquals("3", "(begin 1 2 3)"); }
        @Test void whileProcedure() { assertDevoreEquals("3", "(begin (def x 0) (while (< x 3) (set! x (+ x 1))) x)"); }
    }

    @Nested
    class CoreLogicRandomAndSystem {
        @Test void andProcedure() { assertDevoreTrue("(and true true)"); }
        @Test void orProcedure() { assertDevoreTrue("(or false true)"); }
        @Test void notProcedure() { assertDevoreTrue("(not false)"); }
        @Test void randomStartEnd() { assertDevoreTrue("(>= (random 5 10) 5)"); }
        @Test void randomBounded() { assertDevoreTrue("(< (random 10) 10)"); }
        @Test void type() { assertDevoreEquals("int", "(type 1)"); }
        @Test void time() { assertDevoreTrue("(int? (time))"); }
    }

    @Nested
    class CoreLists {
        @Test void list() { assertDevoreEquals("3", "(length (list 1 2 3))"); }
        @Test void listContains() { assertDevoreTrue("(list-contains (list 1 2 3) 2)"); }
        @Test void listClearMutates() { assertDevoreEquals("0", "(begin (def xs (list 1)) (list-clear! xs) (length xs))"); }
        @Test void listIndex() { assertDevoreEquals("0", "(list-index (list 1 2 1) 1)"); }
        @Test void listIndexLast() { assertDevoreEquals("2", "(list-index-last (list 1 2 1) 1)"); }
        @Test void listGet() { assertDevoreEquals("2", "(list-get (list 1 2 3) 1)"); }
        @Test void listSet() { assertDevoreEquals("9", "(list-get (list-set (list 1 2 3) 1 9) 1)"); }
        @Test void listRemove() { assertDevoreEquals("2", "(length (list-remove (list 1 2 3) 1))"); }
        @Test void listAddAppend() { assertDevoreEquals("4", "(length (list-add (list 1 2 3) 4))"); }
        @Test void listAddAtIndex() { assertDevoreEquals("9", "(list-get (list-add (list 1 2 3) 1 9) 1)"); }
        @Test void listSetMutates() { assertDevoreEquals("9", "(begin (def xs (list 1 2)) (list-set! xs 1 9) (list-get xs 1))"); }
        @Test void listRemoveMutates() { assertDevoreEquals("1", "(begin (def xs (list 1 2)) (list-remove! xs 1) (length xs))"); }
        @Test void listAddMutatesAppend() { assertDevoreEquals("3", "(begin (def xs (list 1 2)) (list-add! xs 3) (length xs))"); }
        @Test void listAddMutatesAtIndex() { assertDevoreEquals("9", "(begin (def xs (list 1 2)) (list-add! xs 1 9) (list-get xs 1))"); }
        @Test void head() { assertDevoreEquals("1", "(head (list 1 2 3))"); }
        @Test void last() { assertDevoreEquals("3", "(last (list 1 2 3))"); }
        @Test void tail() { assertDevoreEquals("2", "(head (tail (list 1 2 3)))"); }
        @Test void init() { assertDevoreEquals("2", "(last (init (list 1 2 3)))"); }
        @Test void length() { assertDevoreEquals("3", "(length (list 1 2 3))"); }
        @Test void listSub() { assertDevoreEquals("2", "(length (list-sub (list 0 1 2 3) 1 3))"); }
        @Test void listSubMutates() { assertDevoreEquals("2", "(begin (def xs (list 0 1 2 3)) (list-sub! xs 1 3) (length xs))"); }
        @Test void reverse() { assertDevoreEquals("3", "(head (reverse (list 1 2 3)))"); }
        @Test void reverseMutates() { assertDevoreEquals("3", "(begin (def xs (list 1 2 3)) (reverse! xs) (head xs))"); }
        @Test void sort() { assertDevoreEquals("1", "(head (sort (list 3 1 2)))"); }
        @Test void sortMutates() { assertDevoreEquals("1", "(begin (def xs (list 3 1 2)) (sort! xs) (head xs))"); }
        @Test void concatenateLists() { assertDevoreEquals("5", "(length (++ (list 1 2) (list 3 4) 5))"); }
        @Test void concatenateStrings() { assertDevoreEquals("abc", "(++ \"a\" \"b\" \"c\")"); }
        @Test void map() { assertDevoreEquals("3", "(head (map (lambda (x) (+ x 1)) (list 2 3)))"); }
        @Test void forEach() { assertDevoreEquals("nil", "(for-each (lambda (x) (+ x 1)) (list 1 2))"); }
        @Test void foldr() { assertDevoreEquals("6", "(foldr + 0 (list 1 2 3))"); }
        @Test void foldl() { assertDevoreEquals("6", "(foldl + 0 (list 1 2 3))"); }
        @Test void filter() { assertDevoreEquals("3", "(head (filter (lambda (x) (> x 2)) (list 1 2 3 4)))"); }
        @Test void rangeOneArg() { assertDevoreEquals("3", "(length (range 3))"); }
        @Test void rangeTwoArgs() { assertDevoreEquals("3", "(length (range 2 5))"); }
        @Test void rangeThreeArgs() { assertDevoreEquals("3", "(length (range 1 7 2))"); }
    }

    @Nested
    class CoreConversionAndTables {
        @Test void stringToSymbol() { assertDevoreEquals("symbol", "(type (string->symbol \"x\"))"); }
        @Test void stringToNumber() { assertDevoreEquals("42", "(string->number \"42\")"); }
        @Test void stringToBool() { assertDevoreEquals("true", "(string->bool \"true\")"); }
        @Test void toStringProcedure() { assertDevoreEquals("42", "(->string 42)"); }
        @Test void stringToList() { assertDevoreEquals("a", "(head (string->list \"ab\"))"); }
        @Test void charToUnicode() { assertDevoreEquals("65", "(char->unicode \"A\")"); }
        @Test void unicodeToChar() { assertDevoreEquals("A", "(unicode->char 65)"); }
        @Test void table() { assertDevoreEquals("2", "(length (table [\"a\" 1] [\"b\" 2]))"); }
        @Test void tableGet() { assertDevoreEquals("1", "(table-get (table [\"a\" 1]) \"a\")"); }
        @Test void tableContainsKey() { assertDevoreTrue("(table-contains-key (table [\"a\" 1]) \"a\")"); }
        @Test void tableContainsValue() { assertDevoreTrue("(table-contains-value (table [\"a\" 1]) 1)"); }
        @Test void tableClearMutates() { assertDevoreEquals("0", "(begin (def t (table [\"a\" 1])) (table-clear! t) (length t))"); }
        @Test void tablePut() { assertDevoreEquals("2", "(table-get (table-put (table [\"a\" 1]) \"b\" 2) \"b\")"); }
        @Test void tablePutMutates() { assertDevoreEquals("2", "(begin (def t (table [\"a\" 1])) (table-put! t \"b\" 2) (table-get t \"b\"))"); }
        @Test void tableRemove() { assertDevoreEquals("0", "(length (table-remove (table [\"a\" 1]) \"a\"))"); }
        @Test void tableRemoveMutates() { assertDevoreEquals("0", "(begin (def t (table [\"a\" 1])) (table-remove! t \"a\") (length t))"); }
        @Test void tableKeys() { assertDevoreEquals("1", "(length (table-keys (table [\"a\" 1])))"); }
    }

    @Nested
    class CorePredicatesAndStrings {
        @Test void max() { assertDevoreEquals("3", "(max 1 3 2)"); }
        @Test void min() { assertDevoreEquals("1", "(min 1 3 2)"); }
        @Test void boolPredicate() { assertDevoreTrue("(bool? true)"); }
        @Test void floatPredicate() { assertDevoreTrue("(float? 1.2)"); }
        @Test void intPredicate() { assertDevoreTrue("(int? 1)"); }
        @Test void listPredicate() { assertDevoreTrue("(list? (list 1))"); }
        @Test void macroPredicate() { assertDevoreTrue("(begin (def-macro (m x) x) (macro? m))"); }
        @Test void boundPredicate() { assertDevoreTrue("(begin (def x 1) (bound? x))"); }
        @Test void exportPredicate() { assertDevoreTrue("(export? (export x))"); }
        @Test void numberPredicate() { assertDevoreTrue("(number? 1.2)"); }
        @Test void procedurePredicate() { assertDevoreTrue("(procedure? +)"); }
        @Test void stringPredicate() { assertDevoreTrue("(string? \"x\")"); }
        @Test void symbolPredicate() { assertDevoreTrue("(symbol? (string->symbol \"x\"))"); }
        @Test void tablePredicate() { assertDevoreTrue("(table? (table [\"a\" 1]))"); }
        @Test void wordPredicate() { assertDevoreTrue("(word? nil)"); }
        @Test void nilPredicate() { assertDevoreTrue("(nil? nil)"); }
        @Test void zeroPredicate() { assertDevoreTrue("(zero? 0)"); }
        @Test void stringSplit() { assertDevoreEquals("b", "(list-get (string-split \"a,b\" \",\") 1)"); }
        @Test void stringTrim() { assertDevoreEquals("a", "(string-trim \" a \")"); }
        @Test void stringTrimLeft() { assertDevoreEquals("a ", "(string-trim-left \" a \")"); }
        @Test void stringTrimRight() { assertDevoreEquals(" a", "(string-trim-right \" a \")"); }
        @Test void stringUpper() { assertDevoreEquals("AB", "(string-upper \"ab\")"); }
        @Test void stringLower() { assertDevoreEquals("ab", "(string-lower \"AB\")"); }
        @Test void stringReplace() { assertDevoreEquals("axb", "(string-replace \"acb\" \"c\" \"x\")"); }
        @Test void stringIndex() { assertDevoreEquals("1", "(string-index \"abcabc\" \"b\")"); }
        @Test void stringIndexLast() { assertDevoreEquals("4", "(string-index-last \"abcabc\" \"b\")"); }
        @Test void stringContains() { assertDevoreTrue("(string-contains \"abc\" \"b\")"); }
        @Test void stringEmptyPredicate() { assertDevoreTrue("(string-empty? \"\")"); }
        @Test void stringStartsWith() { assertDevoreTrue("(string-starts-with \"abc\" \"a\")"); }
        @Test void stringEndsWith() { assertDevoreTrue("(string-ends-with \"abc\" \"c\")"); }
        @Test void stringGet() { assertDevoreEquals("b", "(string-get \"abc\" 1)"); }
        @Test void stringSubTwoArgs() { assertDevoreEquals("bc", "(string-sub \"abc\" 1)"); }
        @Test void stringSubThreeArgs() { assertDevoreEquals("b", "(string-sub \"abc\" 1 2)"); }
    }

    @Nested
    class MathModuleTests {
        @Test void pow() { assertDevoreEquals("8", "(pow 2 3)", "math"); }
        @Test void sqrt() { assertDevoreEquals("9", "(sqrt 81)", "math"); }
        @Test void cbrt() { assertDevoreEquals("3", "(cbrt 27)", "math"); }
        @Test void sin() { assertDevoreTrue("(< (abs (sin 0)) 0.000001)", "math"); }
        @Test void sinh() { assertDevoreTrue("(> (sinh 1) 1)", "math"); }
        @Test void cos() { assertDevoreTrue("(< (abs (- (cos 0) 1)) 0.000001)", "math"); }
        @Test void cosh() { assertDevoreTrue("(> (cosh 1) 1)", "math"); }
        @Test void tan() { assertDevoreTrue("(< (abs (tan 0)) 0.000001)", "math"); }
        @Test void tanh() { assertDevoreTrue("(< (tanh 1) 1)", "math"); }
        @Test void atanOneArg() { assertDevoreTrue("(< (abs (atan 0)) 0.000001)", "math"); }
        @Test void atanTwoArgs() { assertDevoreTrue("(> (atan 0 -1) 3)", "math"); }
        @Test void atanh() { assertDevoreTrue("(> (atanh 0.5) 0)", "math"); }
        @Test void asin() { assertDevoreTrue("(< (abs (asin 0)) 0.000001)", "math"); }
        @Test void asinh() { assertDevoreTrue("(> (asinh 1) 0)", "math"); }
        @Test void acos() { assertDevoreTrue("(< (abs (acos 1)) 0.000001)", "math"); }
        @Test void acosh() { assertDevoreTrue("(> (acosh 2) 0)", "math"); }
        @Test void sech() { assertDevoreTrue("(> (sech 1) 0)", "math"); }
        @Test void csch() { assertDevoreTrue("(> (csch 1) 0)", "math"); }
        @Test void coth() { assertDevoreTrue("(> (coth 1) 0)", "math"); }
        @Test void asech() { assertDevoreTrue("(> (asech 0.5) 0)", "math"); }
        @Test void acsch() { assertDevoreTrue("(> (acsch 1) 0)", "math"); }
        @Test void acoth() { assertDevoreTrue("(> (acoth 2) 0)", "math"); }
        @Test void sec() { assertDevoreTrue("(> (sec 0) 0)", "math"); }
        @Test void csc() { assertDevoreTrue("(> (csc 1) 0)", "math"); }
        @Test void cot() { assertDevoreTrue("(> (cot 1) 0)", "math"); }
        @Test void asec() { assertDevoreTrue("(> (asec 2) 0)", "math"); }
        @Test void acsc() { assertDevoreTrue("(> (acsc 2) 0)", "math"); }
        @Test void acot() { assertDevoreTrue("(> (acot 1) 0)", "math"); }
        @Test void primeDefaultCertainty() { assertDevoreTrue("(prime? 97)", "math"); }
        @Test void primeWithCertainty() { assertDevoreTrue("(prime? 97 20)", "math"); }
        @Test void gcd() { assertDevoreEquals("6", "(gcd 12 18 24)", "math"); }
        @Test void lcm() { assertDevoreEquals("24", "(lcm 6 8)", "math"); }
        @Test void logOneArg() { assertDevoreTrue("(< (abs (- (log (exp 1)) 1)) 0.000001)", "math"); }
        @Test void logTwoArgs() { assertDevoreEquals("1", "(log 10 10)", "math"); }
        @Test void exp() { assertDevoreTrue("(> (exp 1) 2)", "math"); }
        @Test void ceiling() { assertDevoreEquals("3", "(ceiling 2.1)", "math"); }
        @Test void floor() { assertDevoreEquals("2", "(floor 2.9)", "math"); }
        @Test void truncate() { assertDevoreEquals("-2", "(truncate -2.9)", "math"); }
        @Test void round() { assertDevoreEquals("3", "(round 2.6)", "math"); }
    }

    @Nested
    class TextDataModules {
        @Test void htmlEscape() { assertDevoreEquals("&lt;a&gt;", "(html-escape \"<a>\")", "html"); }
        @Test void htmlUnescape() { assertDevoreEquals("<a>", "(html-unescape \"&lt;a&gt;\")", "html"); }
        @Test void jsonRead() { assertDevoreEquals("1", "(table-get (json-read \"{\\\"a\\\":1}\") \"a\")", "json"); }
        @Test void jsonWrite() { assertDevoreEquals("[1,2]", "(json-write (list 1 2))", "json"); }
        @Test void jsonPredicate() { assertDevoreTrue("(json? (table [\"a\" 1]))", "json"); }
        @Test void csvReadString() { assertDevoreEquals("b", "(list-get (head (csv-read-string \"a,b\")) 1)", "csv"); }
        @Test void csvWriteString() { assertDevoreEquals("a,b", "(csv-write-string (list (list \"a\" \"b\")))", "csv"); }
        @Test void propertiesReadString() { assertDevoreEquals("1", "(properties-get (properties-read-string \"a=1\\n\") \"a\")", "properties"); }
        @Test void propertiesWriteString() { assertDevoreTrue("(string-contains (properties-write-string (table [\"a\" \"1\"])) \"a=1\")", "properties"); }
        @Test void propertiesGet() { assertDevoreEquals("nil", "(properties-get (properties-read-string \"a=1\\n\") \"b\")", "properties"); }
        @Test void xmlReadString() { assertDevoreTrue("(xml? (xml-read-string \"<root/>\"))", "xml"); }
        @Test void xmlWriteString() { assertDevoreEquals("<root/>", "(xml-write-string (xml-document (list (xml-element \"root\" (table) (list)))))", "xml"); }
        @Test void xmlDocument() { assertDevoreTrue("(xml? (xml-document (list (xml-element \"root\" (table) (list)))))", "xml"); }
        @Test void xmlElement() { assertDevoreTrue("(xml? (xml-element \"root\" (table) (list)))", "xml"); }
        @Test void xmlText() { assertDevoreTrue("(xml? (xml-text \"text\"))", "xml"); }
        @Test void xmlCdata() { assertDevoreTrue("(xml? (xml-cdata \"text\"))", "xml"); }
        @Test void xmlComment() { assertDevoreTrue("(xml? (xml-comment \"text\"))", "xml"); }
        @Test void xmlPi() { assertDevoreTrue("(xml? (xml-pi \"xml-stylesheet\" \"href='a'\"))", "xml"); }
        @Test void xmlPredicate() { assertDevoreTrue("(xml? (xml-document (list (xml-element \"root\" (table) (list)))))", "xml"); }
    }

    @Nested
    class BinaryBase64HashCryptoSignModules {
        @Test void randomBinary() { assertDevoreEquals("4", "(length (random-binary 4))", "binary"); }
        @Test void stringToBinaryDefaultCharset() { assertDevoreEquals("2", "(length (string->binary \"hi\"))", "binary"); }
        @Test void stringToBinaryNamedCharset() { assertDevoreEquals("2", "(length (string->binary \"hi\" \"UTF-8\"))", "binary"); }
        @Test void binaryToStringDefaultCharset() { assertDevoreEquals("hi", "(binary->string (string->binary \"hi\"))", "binary"); }
        @Test void binaryToStringNamedCharset() { assertDevoreEquals("hi", "(binary->string (string->binary \"hi\" \"UTF-8\") \"UTF-8\")", "binary"); }
        @Test void binaryToHex() { assertDevoreEquals("6869", "(binary->hex (string->binary \"hi\"))", "binary"); }
        @Test void hexToBinary() { assertDevoreEquals("hi", "(binary->string (hex->binary \"6869\"))", "binary"); }
        @Test void base64EncodeString() { assertDevoreEquals("aGk=", "(base64-encode \"hi\")", "base64"); }
        @Test void base64EncodeBinary() { assertDevoreEquals("aGk=", "(base64-encode (string->binary \"hi\"))", "binary", "base64"); }
        @Test void base64Decode() { assertDevoreEquals("hi", "(binary->string (base64-decode \"aGk=\"))", "binary", "base64"); }
        @Test void md5String() { assertDevoreEquals("5d41402abc4b2a76b9719d911017c592", "(md5 \"hello\")", "hash"); }
        @Test void sha1String() { assertDevoreEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", "(sha1 \"hello\")", "hash"); }
        @Test void sha256String() { assertDevoreEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", "(sha256 \"hello\")", "hash"); }
        @Test void sha512String() { assertTrue(eval("(sha512 \"hello\")", "hash").toString().startsWith("9b71d224")); }
        @Test void md5Binary() { assertDevoreEquals("5d41402abc4b2a76b9719d911017c592", "(md5 (string->binary \"hello\"))", "binary", "hash"); }
        @Test void sha1Binary() { assertDevoreEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", "(sha1 (string->binary \"hello\"))", "binary", "hash"); }
        @Test void sha256Binary() { assertDevoreEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", "(sha256 (string->binary \"hello\"))", "binary", "hash"); }
        @Test void sha512Binary() { assertTrue(eval("(sha512 (string->binary \"hello\"))", "binary", "hash").toString().startsWith("9b71d224")); }
        @Test void aesKey() { assertDevoreEquals("16", "(length (aes-key 128))", "crypto"); }
        @Test void aesEncryptDefaultTransformation() { assertDevoreEquals("secret", "(begin (def k (aes-key 128)) (def iv (random-binary 16)) (binary->string (aes-decrypt (aes-encrypt (string->binary \"secret\") k iv) k iv)))", "binary", "crypto"); }
        @Test void aesEncryptExplicitTransformation() { assertDevoreEquals("secret", "(begin (def k (aes-key 128)) (def iv (random-binary 16)) (binary->string (aes-decrypt (aes-encrypt (string->binary \"secret\") k iv \"AES/CBC/PKCS5Padding\") k iv \"AES/CBC/PKCS5Padding\")))", "binary", "crypto"); }
        @Test void rsaKeypairCrypto() { assertDevoreEquals("2", "(length (rsa-keypair 1024))", "crypto"); }
        @Test void rsaEncryptDefault() { assertDevoreEquals("secret", "(begin (def kp (rsa-keypair 1024)) (binary->string (rsa-decrypt (rsa-encrypt (string->binary \"secret\") (table-get kp \"public\")) (table-get kp \"private\"))))", "binary", "crypto"); }
        @Test void rsaEncryptWithTransformation() { assertDevoreEquals("secret", "(begin (def kp (rsa-keypair 1024)) (binary->string (rsa-decrypt (rsa-encrypt (string->binary \"secret\") (table-get kp \"public\") \"RSA/ECB/PKCS1Padding\") (table-get kp \"private\") \"RSA/ECB/PKCS1Padding\")))", "binary", "crypto"); }
        @Test void rsaKeypairSign() { assertDevoreEquals("2", "(length (rsa-keypair 1024))", "sign"); }
        @Test void ecdsaKeypairDefault() { assertDevoreEquals("2", "(length (ecdsa-keypair))", "sign"); }
        @Test void ecdsaKeypairNamedCurve() { assertDevoreEquals("2", "(length (ecdsa-keypair \"secp256r1\"))", "sign"); }
        @Test void rsaSignDefault() { assertDevoreTrue("(begin (def kp (rsa-keypair 1024)) (def data (string->binary \"hello\")) (rsa-verify data (rsa-sign data (table-get kp \"private\")) (table-get kp \"public\")))", "binary", "sign"); }
        @Test void rsaSignWithAlgorithm() { assertDevoreTrue("(begin (def kp (rsa-keypair 1024)) (def data (string->binary \"hello\")) (rsa-verify data (rsa-sign data (table-get kp \"private\") \"SHA256withRSA\") (table-get kp \"public\") \"SHA256withRSA\"))", "binary", "sign"); }
        @Test void ecdsaSignDefault() { assertDevoreTrue("(begin (def kp (ecdsa-keypair)) (def data (string->binary \"hello\")) (ecdsa-verify data (ecdsa-sign data (table-get kp \"private\")) (table-get kp \"public\")))", "binary", "sign"); }
        @Test void ecdsaSignWithAlgorithm() { assertDevoreTrue("(begin (def kp (ecdsa-keypair)) (def data (string->binary \"hello\")) (ecdsa-verify data (ecdsa-sign data (table-get kp \"private\") \"SHA256withECDSA\") (table-get kp \"public\") \"SHA256withECDSA\"))", "binary", "sign"); }
    }

    @Nested
    class RegexUuidTimeOsReflectSecurityModules {
        @Test void regexMatch() { assertDevoreTrue("(regex-match \"a.*\" \"abc\")", "regex"); }
        @Test void regexFind() { assertDevoreEquals("abc", "(head (regex-find \"a.*\" \"abc\"))", "regex"); }
        @Test void regexFindAll() { assertDevoreEquals("2", "(length (regex-find-all \"a\" \"a a\"))", "regex"); }
        @Test void regexReplace() { assertDevoreEquals("xbc", "(regex-replace \"a\" \"abc\" \"x\")", "regex"); }
        @Test void regexSplit() { assertDevoreEquals("b", "(list-get (regex-split \",\" \"a,b\") 1)", "regex"); }
        @Test void regexQuote() { assertDevoreTrue("(regex-match (regex-quote \"a.b\") \"a.b\")", "regex"); }
        @Test void uuid() { assertDevoreEquals("36", "(length (uuid))", "uuid"); }
        @Test void uuidSimpleNoArg() { assertDevoreEquals("32", "(length (uuid-simple))", "uuid"); }
        @Test void uuidParse() { assertDevoreEquals("123e4567-e89b-12d3-a456-426614174000", "(uuid-parse \"123e4567e89b12d3a456426614174000\")", "uuid"); }
        @Test void uuidSimpleFromUuid() { assertDevoreEquals("123e4567e89b12d3a456426614174000", "(uuid-simple \"123e4567-e89b-12d3-a456-426614174000\")", "uuid"); }
        @Test void formatTimeDefaultPattern() { assertDevoreEquals("19", "(string-sub (format-time 0) 0 2)", "time"); }
        @Test void formatTimeCustomPattern() { assertDevoreEquals("1970-01-01", "(format-time 0 \"yyyy-MM-dd\")", "time"); }
        @Test void parseTimeDefaultPattern() { assertDevoreTrue("(int? (parse-time (format-time 0)))", "time"); }
        @Test void parseTimeCustomPattern() { assertDevoreTrue("(int? (parse-time \"1970-01-01\" \"yyyy-MM-dd\"))", "time"); }
        @Test void osName() { assertDevoreTrue("(string? (os-name))", "os"); }
        @Test void osArch() { assertDevoreTrue("(string? (os-arch))", "os"); }
        @Test void osVersion() { assertDevoreTrue("(string? (os-version))", "os"); }
        @Test void osUserName() { assertDevoreTrue("(string? (os-user-name))", "os"); }
        @Test void osUserHome() { assertDevoreTrue("(string? (os-user-home))", "os"); }
        @Test void osCurrentDir() { assertDevoreTrue("(string? (os-current-dir))", "os"); }
        @Test void osLineSeparator() { assertDevoreTrue("(string? (os-line-separator))", "os"); }
        @Test void osAvailableProcessors() { assertDevoreTrue("(int? (os-available-processors))", "os"); }
        @Test void osFreeMemory() { assertDevoreTrue("(int? (os-free-memory))", "os"); }
        @Test void osTotalMemory() { assertDevoreTrue("(int? (os-total-memory))", "os"); }
        @Test void osMaxMemory() { assertDevoreTrue("(int? (os-max-memory))", "os"); }
        @Test void osProcessId() { assertDevoreTrue("(int? (os-process-id))", "os"); }
        @Test void osEnv() { assertDevoreTrue("(or (string? (os-env \"PATH\")) (nil? (os-env \"PATH\")))", "os"); }
        @Test void osEnvs() { assertDevoreTrue("(table? (os-envs))", "os"); }
        @Test void osProperty() { assertDevoreTrue("(string? (os-property \"java.version\"))", "os"); }
        @Test void osProperties() { assertDevoreTrue("(table? (os-properties))", "os"); }
        @Test void javaObjectPredicate() { assertDevoreTrue("(java-object? (reflect-class \"java.lang.String\"))", "reflect"); }
        @Test void reflectClass() { assertDevoreEquals("java.lang.String", "(reflect-class-name (reflect-class \"java.lang.String\"))", "reflect"); }
        @Test void reflectClassName() { assertDevoreEquals("java.lang.String", "(reflect-class-name (reflect-class \"java.lang.String\"))", "reflect"); }
        @Test void reflectNew() { assertDevoreTrue("(java-object? (reflect-new \"java.lang.StringBuilder\" \"abc\"))", "reflect"); }
        @Test void reflectCall() { assertDevoreEquals("3", "(reflect-call (reflect-new (reflect-class \"java.lang.String\") \"abc\") \"length\")", "reflect"); }
        @Test void reflectStaticCall() { assertDevoreEquals("5", "(reflect-static-call (reflect-class \"java.lang.Integer\") \"parseInt\" \"5\")", "reflect"); }
        @Test void security() { assertDevoreEquals("1", "(begin (security \"file\") (length (security-restrictions)))", "security"); }
        @Test void securityClear() { assertDevoreEquals("0", "(begin (security \"file\") (security-clear!) (length (security-restrictions)))", "security"); }
        @Test void securityRemove() { assertDevoreTrue("(begin (security \"file\") (security-remove! \"file\") (not (security-restrict? \"file\")))", "security"); }
        @Test void securityRestrictions() { assertDevoreTrue("(list? (security-restrictions))", "security"); }
        @Test void securityRestrictPredicate() { assertDevoreTrue("(begin (security \"file\") (security-restrict? \"file\"))", "security"); }
    }
}
