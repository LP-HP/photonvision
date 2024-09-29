import pybind11_stubgen
from pybind11_stubgen.structs import InvalidExpression, Value


def hack_stubgen():
    from pybind11_stubgen import (
        CLIArgs,
        IParser,
        LoggerData,
        IgnoreAllErrors,
        IgnoreInvalidIdentifierErrors,
        IgnoreInvalidExpressionErrors,
        IgnoreUnresolvedNameErrors,
        LogErrors,
        SuggestCxxSignatureFix,
        TerminateOnFatalErrors,
        FixNumpyArrayDimAnnotation,
        FixNumpyArrayDimTypeVar,
        FixNumpyArrayFlags,
        FixNumpyArrayRemoveParameters,
        FixMissing__future__AnnotationsImport,
        FixMissing__all__Attribute,
        FixMissingNoneHashFieldAnnotation,
        FixMissingImports,
        FilterTypingModuleAttributes,
        FixPEP585CollectionNames,
        FixTypingTypeNames,
        FixScipyTypeArguments,
        FixMissingFixedSizeImport,
        FixMissingEnumMembersAnnotation,
        OverridePrintSafeValues,
        FixNumpyDtype,
        FixNumpyArrayFlags,
        FixCurrentModulePrefixInTypeNames,
        FixBuiltinTypes,
        RewritePybind11EnumValueRepr,
        FilterClassMembers,
        ReplaceReadWritePropertyWithField,
        FilterInvalidIdentifiers,
        FixValueReprRandomAddress,
        FixRedundantBuiltinsAnnotation,
        FilterPybindInternals,
        FilterPybind11ViewClasses,
        FixRedundantMethodsFromBuiltinObject,
        RemoveSelfAnnotation,
        FixPybind11EnumStrDoc,
        ExtractSignaturesFromPybind11Docstrings,
        ParserDispatchMixin,
        BaseParser,
    )
    from pybind11_stubgen.structs import QualifiedName, Docstring, Alias, Class, Method, Field, Property, Identifier, Argument, ResolvedType
    from typing import Any

    class FixWpilibTypestrings(IParser):
        def parse_value_str(self, value: str) -> Value | InvalidExpression:
            # TODO
            if value.startswith("frc::"):
                return Value(value, is_print_safe=False)
            return super().parse_value_str(value)

    def stub_parser_from_args_HACK(args: CLIArgs) -> IParser:
        error_handlers_top: list[type] = [
            LoggerData,
            *([IgnoreAllErrors] if args.ignore_all_errors else []),
            *(
                [IgnoreInvalidIdentifierErrors]
                if args.ignore_invalid_identifiers
                else []
            ),
            *(
                [IgnoreInvalidExpressionErrors]
                if args.ignore_invalid_expressions
                else []
            ),
            *([IgnoreUnresolvedNameErrors] if args.ignore_unresolved_names else []),
        ]
        error_handlers_bottom: list[type] = [
            LogErrors,
            SuggestCxxSignatureFix,
            *([TerminateOnFatalErrors] if args.exit_code else []),
        ]

        numpy_fixes: list[type] = [
            *(
                [FixNumpyArrayDimAnnotation]
                if args.numpy_array_wrap_with_annotated
                else []
            ),
            *([FixNumpyArrayDimTypeVar] if args.numpy_array_use_type_var else []),
            *(
                [FixNumpyArrayRemoveParameters]
                if args.numpy_array_remove_parameters
                else []
            ),
        ]

        class Parser(
            *error_handlers_top,  # type: ignore[misc]
            FixMissing__future__AnnotationsImport,
            FixMissing__all__Attribute,
            FixMissingNoneHashFieldAnnotation,
            FixMissingImports,
            FilterTypingModuleAttributes,
            FixPEP585CollectionNames,
            FixTypingTypeNames,
            FixScipyTypeArguments,
            FixMissingFixedSizeImport,
            FixMissingEnumMembersAnnotation,
            OverridePrintSafeValues,
            FixWpilibTypestrings,
            *numpy_fixes,  # type: ignore[misc]
            FixNumpyDtype,
            FixNumpyArrayFlags,
            FixCurrentModulePrefixInTypeNames,
            FixBuiltinTypes,
            RewritePybind11EnumValueRepr,
            FilterClassMembers,
            ReplaceReadWritePropertyWithField,
            FilterInvalidIdentifiers,
            FixValueReprRandomAddress,
            FixRedundantBuiltinsAnnotation,
            FilterPybindInternals,
            FilterPybind11ViewClasses,
            FixRedundantMethodsFromBuiltinObject,
            RemoveSelfAnnotation,
            FixPybind11EnumStrDoc,
            ExtractSignaturesFromPybind11Docstrings,
            ParserDispatchMixin,
            BaseParser,
            *error_handlers_bottom,  # type: ignore[misc]
        ):
            pass

        parser = Parser()

        if args.enum_class_locations:
            parser.set_pybind11_enum_locations(dict(args.enum_class_locations))
        if args.ignore_invalid_identifiers is not None:
            parser.set_ignored_invalid_identifiers(args.ignore_invalid_identifiers)
        if args.ignore_invalid_expressions is not None:
            parser.set_ignored_invalid_expressions(args.ignore_invalid_expressions)
        if args.ignore_unresolved_names is not None:
            parser.set_ignored_unresolved_names(args.ignore_unresolved_names)
        if args.print_safe_value_reprs is not None:
            parser.set_print_safe_value_pattern(args.print_safe_value_reprs)
        return parser

    # muahahaha
    pybind11_stubgen.stub_parser_from_args = stub_parser_from_args_HACK


def write_stubgen():
    import os

    script_path = os.path.dirname(os.path.realpath(__file__))

    # mess with argv (and put it back when we're done)
    import sys

    old_argv = sys.argv
    sys.argv = [
        "<dummy>",
        "--exit-code",
        "--ignore-invalid-expressions=<.*>",
        "--root-suffix=",
        "-o",
        f"{script_path}",
        "photonlibpy",
    ]

    try:
        hack_stubgen()
        pybind11_stubgen.main()
    except Exception as e:
        print(e)

    sys.argv = old_argv


if __name__ == "__main__":
    write_stubgen()
