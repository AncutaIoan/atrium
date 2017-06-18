package ch.tutteli.atrium.reporting

import ch.tutteli.atrium.IAtriumFactory
import ch.tutteli.atrium.assertions.IAssertion

/**
 * Responsible to call an appropriate [IAssertionFormatter] which supports [format]ing a given [IAssertion].
 */
interface IAssertionFormatterFacade {
    /**
     * Formats the given [assertion] and appends the result to the given [sb].
     *
     * One can define an [assertionFilter] to filter out [IAssertion]s
     * (for instance, filter out assertions which hold &rarr; see [IAtriumFactory.newOnlyFailureReporter]).
     *
     */
    fun format(assertion: IAssertion, sb: StringBuilder, assertionFilter: (IAssertion) -> Boolean)

    /**
     * Uses the given [assertionFormatterFactory] to create and register an [IAssertionFormatter] -- which means
     * the created [IAssertionFormatter] will be considered when an [IAssertion] shall be [format]ted.
     *
     * @param assertionFormatterFactory The factory method to create an [IAssertionFormatter] which shall be registered.
     */
    fun register(assertionFormatterFactory: (IAssertionFormatterController) -> IAssertionFormatter)
}
