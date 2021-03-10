#!/bin/bash

_bal_completions()
{
    flagVar=" -- "
    for ((i = 1; i < ${#COMP_WORDS[@]}; i++))
    do
        flagVar="$flagVar ${COMP_WORDS[$i]}"
    done
    balCommands=$(bal compgen $flagVar 2>/dev/null)

    if [ $? == '0' ]; then
         COMPREPLY=($(compgen -W "$balCommands"))
         return
    fi
    COMPREPLY=($(compgen -o plusdirs -f -X '!*.bal' -- "${COMP_WORDS[COMP_CWORD]}"))
} 
 
complete -o nospace -F _bal_completions bal
