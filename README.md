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
