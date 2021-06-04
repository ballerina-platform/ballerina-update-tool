# Get the file path based on OS type
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    dists_path=/usr/lib/ballerina/distributions
elif [[ "$OSTYPE" == "darwin"* ]]; then
    dists_path=/Library/Ballerina/distributions
fi

_bal_completions()
{
    balCommands="-v --version add build clean dist doc format help new pull push run test update version"
    dist="list pull remove update use"

    # Get current ballerina version
    if test -f "$HOME/.ballerina/ballerina-version"; then
        bal_version=$(cat $HOME/.ballerina/ballerina-version)
    else
        bal_version=$(cat $dists_path/ballerina-version)
    fi

    file_path=$dists_path/$bal_version/resources/command-completion/command-completion.csv

    if test -f "$file_path"; then
        # Read the optional flags from command-completion file
        while IFS=, read -r cmdname flags
        do
            if [[ "${COMP_WORDS[1]}" = "$cmdname" ]]; then
                flags_arr="$flags"
            elif [[ "$cmdname" == "bal" ]]; then
                balCommands="$flags"
            fi
        done < <(tail -n +2 $file_path)
    fi

    if [[ "$COMP_CWORD" == "1" ]]; then
        COMPREPLY=($(compgen -W "$balCommands -h --help" -- "${COMP_WORDS[$COMP_CWORD]}"))
        return
    else
        if [[ ("${COMP_WORDS[1]}" = "dist") ]]; then
            COMPREPLY=($(compgen -W "$dist -h --help" "${COMP_WORDS[$COMP_CWORD]}"))
            return
        elif [[ "${COMP_WORDS[$COMP_CWORD]}" =~ ^\-.* ]] && [ -f "$file_path" ]; then
            # If last word has - we will suggest flags.
            COMPREPLY=($(compgen -W "${flags_arr[@]} -h --help" -- "${COMP_WORDS[$COMP_CWORD]}"))
            return
        fi
    fi
}

complete -o bashdefault -o default -F _bal_completions bal
