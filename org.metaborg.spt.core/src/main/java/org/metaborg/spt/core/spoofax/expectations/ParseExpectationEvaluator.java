package org.metaborg.spt.core.spoofax.expectations;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spt.core.IFragment;
import org.metaborg.spt.core.ITestCase;
import org.metaborg.spt.core.ITestExpectationInput;
import org.metaborg.spt.core.ITestExpectationOutput;
import org.metaborg.spt.core.TestExpectationOutput;
import org.metaborg.spt.core.TestPhase;
import org.metaborg.spt.core.expectations.MessageUtil;
import org.metaborg.spt.core.expectations.ParseExpectation;
import org.metaborg.spt.core.spoofax.ISpoofaxExpectationEvaluator;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.strategoxt.lang.TermEqualityUtil;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ParseExpectationEvaluator implements ISpoofaxExpectationEvaluator<ParseExpectation> {

    private static final ILogger logger = LoggerUtils.logger(ParseExpectationEvaluator.class);

    private final FragmentUtil fragmentUtil;
    private final ITermFactoryService termFactoryService;

    @Inject public ParseExpectationEvaluator(FragmentUtil fragmentUtil, ITermFactoryService termFactoryService) {
        this.fragmentUtil = fragmentUtil;
        this.termFactoryService = termFactoryService;
    }

    @Override public Collection<Integer> usesSelections(IFragment fragment, ParseExpectation expectation) {
        return Lists.newLinkedList();
    }

    @Override public TestPhase getPhase(IContext languageUnderTestCtx, ParseExpectation expectation) {
        return TestPhase.PARSING;
    }

    @Override public ITestExpectationOutput
        evaluate(ITestExpectationInput<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit> input, ParseExpectation expectation) {
        ISpoofaxParseUnit p = input.getParseResult();
        ITestCase test = input.getTestCase();
        final boolean success;

        List<IMessage> messages = new LinkedList<>();

        if(expectation.outputFragment() == null) {
            // this is a parse fails or succeeds test
            success = p.success() == expectation.successExpected();
            if(!success) {
                final String msg =
                    expectation.successExpected() ? "Expected parsing to succeed" : "Expected a parse failure";
                messages
                    .add(MessageFactory.newAnalysisError(test.getResource(), test.getDescriptionRegion(), msg, null));
                if(expectation.successExpected()) {
                    // propagate the parse messages
                    MessageUtil.propagateMessages(p.messages(), messages, test.getDescriptionRegion(),
                        test.getFragment().getRegion());
                }
            }
        } else {
            // this is 'parse to'
            logger.debug("Evaluating a parse to expectation (expect success: {}, lang: {}, fragment: {}).",
                expectation.successExpected(), expectation.outputLanguage(), expectation.outputFragment());
            if(!p.success()) {
                messages.add(MessageFactory.newAnalysisError(test.getResource(), test.getDescriptionRegion(),
                    "Expected parsing to succeed", null));
                // propagate the parse messages
                MessageUtil.propagateMessages(p.messages(), messages, test.getDescriptionRegion(),
                    test.getFragment().getRegion());
            }

            // parse the output fragment
            ISpoofaxParseUnit parsedFragment =
                fragmentUtil.parseFragment(expectation.outputFragment(), expectation.outputLanguage(), messages, test);

            // compare the results and set the success boolean
            if(parsedFragment == null) {
                success = false;
            } else {
                ILanguage outputLang = fragmentUtil.getLanguage(expectation.outputLanguage(), messages, test);
                if(outputLang != null && !TermEqualityUtil.equalsIgnoreAnnos(p.ast(), parsedFragment.ast(),
                    termFactoryService.get(outputLang.activeImpl()))) {
                    // TODO: add a nice diff of the two parse results or something
                    messages.add(MessageFactory.newAnalysisError(test.getResource(), test.getDescriptionRegion(),
                        "The expected parse result did not match the actual parse result", null));
                }
                success = messages.isEmpty();
            }
        }

        return new TestExpectationOutput(success, messages);
    }

}
