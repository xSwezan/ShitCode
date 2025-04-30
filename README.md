<div align="center">

<img width="256" height="256" src="assets/icon.svg" />

### Shitty scripting language designed for inconvenience

</div>

## General information and motivation

This is a scripting language made in Java for a school project. When designing the language; I wanted something easy to use, but "inconvenient". Simple logic may require a tad more code than your average language. The main focus was to make it easy to understand (if you understand english), NOT to be easy to manage.

### Language features
- Variable declaration
- Function declaration
- Math
- Boolean operations (and, or, not, <, >, <=, >=, ==, !=)
- If statements
- While statements
- Loop statements
- Bundles

### Java features
- Sandboxed environment
- Global variables
- Native functions

### Unit tests
The language is mostly unit tested (coverage not calculated). These unit tests are AI-generated and are not thoroughly checked, because I'm lazy. Tests can be found in [./units](https://github.com/xSwezan/ShitCode/tree/main/units).


### Code examples

<details>
<summary>Variable declaration</summary>

```
create number called foo
set foo to 10

create number called bar
set bar to foo * 2
```
</details>



<details>
<summary>Function declaration</summary>

```
create function called add using number a, number b {
    return a + b
}

create number called sum
set sum to call add with 1, 5

call talk with "Sum is: " + sum
```
</details>

> [!TIP]
> More code examples can be found in the [unit tests](https://github.com/xSwezan/ShitCode/tree/main/units)

### How it works (simplified)

The program can be shortened down to 3 parts:
- Lexilizing
- Parsing
- Interpreting

The whole program starts with a string, a raw string containing the code you want to run. This string is sent through a *Lexilizer*, which converts this raw string to a list of tokens. These tokens have different types and contain the raw string. A token is basically a group of characters, this might be a keyword, string, number, or an identifier. The tokens are sent to a *Parser*, which parses the tokens, and produces expressions and statements of different kinds. Among others, it may create: IfStatement, WhileStatement, RepeatStatement, CallExpression, BinaryExpression, or UnaryExpression. These are placed in a tree-like structure, where the code begins with a Body, and the body contains Nodes (which is a Statement or an Expression). The *Interpreter* then traverses this tree in chronological order and performs the necessary logic. This might be for example variable declaration, if statements, member access, and more.
