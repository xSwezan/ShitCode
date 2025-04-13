# and, or
# List
```
create list called myList
append "Hello" to myList

create number called index
set index to find "Hello" in myList
remove index from myList
```

# Bundle (like a table)
```
create function called newPlayer {
    create bundle called player

    create number called health in player
    set player->health to 1000

    create function called takeDamage in player using number damage {
        set player->health to player->health - damage
    }

    return player
}

create bundle called player
set player to call newPlayer

call talk with "Player health: " + player->health
call player->takeDamage with 10
call talk with "Player health: " + player->health
```

# Imports
Imports another script as a bundle
```
import "test.shit" as test
```

# Typechecking