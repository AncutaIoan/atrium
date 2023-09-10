package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.api.infix.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.core.ExperimentalNewExpectTypes
import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.creating.ComponentFactoryContainer
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.ExperimentalComponentFactoryContainer
import ch.tutteli.atrium.creating.build
import ch.tutteli.atrium.logic._logic
import ch.tutteli.atrium.logic.creating.RootExpectBuilder
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import kotlin.test.Test

class AdjustStackTest {

    @Test
    fun noOp_containsMochaAndAtrium() {
        expect {
            assertNoOp(1) toEqual 2
        }.toThrow<AssertionError> {
            feature(AssertionError::stackBacktrace) toContain entries(
                { it toContain Regex("""[\\|/]node_modules[\\|/]mocha[\\|/]""") },
                { it toContain "createAtriumError" }
            )
        }
    }

    @Test
    fun noOp_makeSureStackBacktraceIsInOutput_issue1383() {
        expect{
            expect {
                assertNoOp(1) toEqual 2
            }.toThrow<AssertionError> {
                feature(AssertionError::stackBacktrace) toContain entries(
                    { it toContain "createAtriumError2" }
                )
            }
        }.toThrow<AssertionError> {
            it messageToContain "stackBacktrace"
        }
    }

    @Test
    fun removeRunner_containsAtriumButNotMocha() {
        expect {
            assertRemoveRunner(1) toEqual 2
        }.toThrow<AssertionError> {
            it feature of(AssertionError::stackBacktrace) {
                it notToContain o entry { it toContain "mocha" }
                it notToContain o entry { it toContain "KotlinTestTeamCityConsoleAdapter" }
                it toContain { it toContain "createAtriumError" }
            }
        }
    }

    @Test
    fun removeRunner_containsAtriumButNotMochaInCause() {
        assertRemoveRunner {
            throw IllegalArgumentException("hello", UnsupportedOperationException("world"))
        }.toThrow<IllegalArgumentException> {
            cause<UnsupportedOperationException> {
                it feature of(UnsupportedOperationException::stackBacktrace) {
                    it notToContain o entry { it toContain "mocha" }
                    it notToContain o entry { it toContain "KotlinTestTeamCityConsoleAdapter" }
                    it toContain { it toContain Regex("toThrow.*atrium-logic") }
                }
            }
        }
    }

    @Test
    fun removeAtrium_containsMochaButNotAtrium() {
        expect {
            assertRemoveAtrium(1) toEqual 2
        }.toThrow<AssertionError> {
            it feature of(AssertionError::stackBacktrace) {
                it toContain { it toContain "mocha" }
                it notToContain o entry { it toContain "atrium-core" }
            }
        }
    }

    @ExperimentalComponentFactoryContainer
    @Test
    fun removeAtrium_containsMochaButNotAtriumInCause() {
        val adjuster = assertRemoveAtrium(1)._logic.components.build<AtriumErrorAdjuster>()
        expect(adjuster).toBeAnInstanceOf<RemoveAtriumFromAtriumError>()
        val throwable = IllegalArgumentException("hello", UnsupportedOperationException("world"))
        adjuster.adjust(throwable)
        expect(throwable.cause!!.stackBacktrace) {
            it toContain (fun Expect<String>.() {
                it toContain "mocha"
            })
            it notToContain o entry { it toContain "atrium-core-js" }
        }
    }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertNoOp(subject: T) = createExpect(subject) { NoOpAtriumErrorAdjuster }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertRemoveRunner(subject: T) =
        createExpect(subject) { c -> c.build<RemoveRunnerFromAtriumError>() }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertRemoveAtrium(subject: T) =
        createExpect(subject) { c -> c.build<RemoveAtriumFromAtriumError>() }


    @ExperimentalNewExpectTypes
    @ExperimentalComponentFactoryContainer
    private fun <T : Any> createExpect(subject: T, factory: (ComponentFactoryContainer) -> AtriumErrorAdjuster) =
        RootExpectBuilder.forSubject(subject)
            .withVerb("I expected subject")
            .withOptions {
                withComponent(AtriumErrorAdjuster::class, factory)
            }
            .build()
}
