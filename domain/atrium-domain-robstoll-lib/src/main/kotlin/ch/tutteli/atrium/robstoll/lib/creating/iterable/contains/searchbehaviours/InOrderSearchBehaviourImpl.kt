package ch.tutteli.atrium.robstoll.lib.creating.iterable.contains.searchbehaviours

import ch.tutteli.atrium.domain.creating.iterable.contains.searchbehaviours.InOrderSearchBehaviour
import ch.tutteli.atrium.reporting.translating.Translatable
import ch.tutteli.atrium.reporting.translating.TranslatableWithArgs
import ch.tutteli.atrium.translations.DescriptionIterableAssertion

/**
 * Represents the search behaviour that expected entries have to appear in the given order within the [Iterable].
 */
class InOrderSearchBehaviourImpl : InOrderSearchBehaviour {
    override fun decorateDescription(description: Translatable): Translatable
        = TranslatableWithArgs(DescriptionIterableAssertion.IN_ORDER, description)
}
