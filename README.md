# CausalExtraction
Extraction of Causal Expressions using clue phrases


# Requirements
* Linux or Mac or Windows10(64bit)
* [CaboCha](https://code.google.com/p/cabocha/)
    * UTF-8
* JAVA 1.8 (oracle)
* ant (for making jar file)

# Install
```
$ ant
$ ant jar
```
A jar file(extractCausal.jar) is made in the sample folder.

# Usage
## Example
You move to the sample folder.
Then, run the following command.

`$ java -Dfile.encoding=UTF-8 -Xms2G -Xmx8G -jar extractCausal.jar test_list.txt`

## Argments
* File list for extracting causal expressions

## Additional Argments
* -p, --pattern <arg>    use Prefix Patterns
* -s, --svm <arg>        use SVM results
* -t, --thredNum <arg>   Thread Number

### Example 2
`$ java -Dfile.encoding=UTF-8 -Xms2G -Xmx8G -jar extractCausal.jar -t 4 -s svm_result.txt -p additional_data.txt test_list.txt`

## Results
CausalExtraction program outputs a following json.

`
{"clue": "で、", "basis": "円高による不況の影響", "result": "買い物客が激減。", "subj": "", "pattern": "A", "filePath": "test1.txt", "line": 2}
`

# LICENSE
```text
The MIT License (MIT)

Copyright (c) 2014 Hiroki Sakaji

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

# References
1. 坂地泰紀, 酒井浩之, 増山繁, 決算短信PDFからの原因・結果表現の抽出, 電子情報通信学会論文誌D, Vol.J98-D, No.5, pp.811-822, 2015.
2. 坂地泰紀, 増山繁, 新聞記事からの因果関係を含む文の抽出手法, 電子情報通信学会論文誌D, Vol.J94-D, No.8, pp.1496-1506, 2011.
3. Hiroki Sakaji, Satoshi Sekine, Shigeru Masuyama, Extracting Causal Knowledge Using Clue Phrases and Syntactic Patterns, 7th International Conference on Practical Aspects of Knowledge Management (PAKM), pp.111-122, Yokohama, Japan, 2008.
4. 坂地泰紀, 竹内康介, 関根聡, 増山繁, 構文パターンを用いた因果関係の抽出, 言語処理学会第14回年次大会, pp.1144-1147, 2008.

# Acknowledgement
The following people found bugs of this program.
Thank you very much.
* 佐藤史仁 (日興リサーチセンター株式会社) 様
* 田中良典 (日興リサーチセンター株式会社) 様
