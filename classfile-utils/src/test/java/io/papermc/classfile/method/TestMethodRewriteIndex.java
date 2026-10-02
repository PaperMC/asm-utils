package io.papermc.classfile.method;

import io.papermc.classfile.ClassFiles;
import io.papermc.classfile.method.action.DirectStaticCall;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;
import org.junit.jupiter.api.Test;

import static io.papermc.classfile.method.MethodDescriptorPredicate.hasReturn;
import static io.papermc.classfile.method.MethodNamePredicate.constructor;
import static io.papermc.classfile.method.MethodNamePredicate.exact;
import static io.papermc.classfile.method.MethodNamePredicate.prefix;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TestMethodRewriteIndex {

    private static final ClassDesc OWNER_A = ClassDesc.of("com.example.A");
    private static final ClassDesc OWNER_B = ClassDesc.of("com.example.B");

    private static MethodRewrite exactRewrite(final ClassDesc owner, final String methodName) {
        return new MethodRewrite(owner, exact(methodName), hasReturn(ConstantDescs.CD_void), mock(DirectStaticCall.class));
    }

    private static MethodRewrite wildcardRewrite(final ClassDesc owner) {
        return new MethodRewrite(owner, prefix("get"), hasReturn(ConstantDescs.CD_void), mock(DirectStaticCall.class));
    }

    private static MethodRewrite constructorRewrite(final ClassDesc owner) {
        return new MethodRewrite(owner, constructor(), hasReturn(ConstantDescs.CD_void), mock(DirectStaticCall.class));
    }

    @Test
    void noRewritesReturnsEmpty() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of());
        assertThat(index.candidates(OWNER_A, "foo")).isEmpty();
    }

    @Test
    void exactMatchReturnsRewrite() {
        final MethodRewrite rewrite = exactRewrite(OWNER_A, "foo");
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(rewrite));
        assertThat(index.candidates(OWNER_A, "foo")).containsExactly(rewrite);
    }

    @Test
    void exactMatchWrongNameReturnsEmpty() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exactRewrite(OWNER_A, "foo")));
        assertThat(index.candidates(OWNER_A, "bar")).isEmpty();
    }

    @Test
    void exactMatchWrongOwnerReturnsEmpty() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exactRewrite(OWNER_A, "foo")));
        assertThat(index.candidates(OWNER_B, "foo")).isEmpty();
    }

    @Test
    void wildcardMatchesByOwnerRegardlessOfMethodName() {
        final MethodRewrite rewrite = wildcardRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(rewrite));
        assertThat(index.candidates(OWNER_A, "foo")).containsExactly(rewrite);
        assertThat(index.candidates(OWNER_A, "bar")).containsExactly(rewrite);
    }

    @Test
    void wildcardWrongOwnerReturnsEmpty() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(wildcardRewrite(OWNER_A)));
        assertThat(index.candidates(OWNER_B, "foo")).isEmpty();
    }

    @Test
    void exactAndWildcardForSameOwnerBothReturned() {
        final MethodRewrite exact = exactRewrite(OWNER_A, "foo");
        final MethodRewrite wildcard = wildcardRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exact, wildcard));
        assertThat(index.candidates(OWNER_A, "foo")).containsExactly(exact, wildcard);
    }

    @Test
    void exactDoesNotReturnForNonMatchingNameWhenWildcardPresent() {
        final MethodRewrite exact = exactRewrite(OWNER_A, "foo");
        final MethodRewrite wildcard = wildcardRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exact, wildcard));
        // wildcard matches, exact does not
        assertThat(index.candidates(OWNER_A, "bar")).containsExactly(wildcard);
    }

    @Test
    void multipleExactMatchesSameOwnerAndName() {
        final MethodRewrite first = exactRewrite(OWNER_A, "foo");
        final MethodRewrite second = exactRewrite(OWNER_A, "foo");
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(first, second));
        assertThat(index.candidates(OWNER_A, "foo")).containsExactly(first, second);
    }

    @Test
    void differentOwnersDoNotInterfere() {
        final MethodRewrite rewriteA = exactRewrite(OWNER_A, "foo");
        final MethodRewrite rewriteB = exactRewrite(OWNER_B, "foo");
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(rewriteA, rewriteB));
        assertThat(index.candidates(OWNER_A, "foo")).containsExactly(rewriteA);
        assertThat(index.candidates(OWNER_B, "foo")).containsExactly(rewriteB);
    }

    @Test
    void orderIsPreservedExactBeforeWildcard() {
        final MethodRewrite exact = exactRewrite(OWNER_A, "foo");
        final MethodRewrite wildcard = wildcardRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exact, wildcard));
        final List<MethodRewrite> candidates = index.candidates(OWNER_A, "foo");
        assertThat(candidates.indexOf(exact)).isLessThan(candidates.indexOf(wildcard));
    }

    // --- hasConstructorRewrites ---

    @Test
    void hasConstructorRewritesReturnsFalseWhenEmpty() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of());
        assertThat(index.hasConstructorRewrites()).isFalse();
    }

    @Test
    void hasConstructorRewritesReturnsFalseWhenOnlyNonConstructorRewrites() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(exactRewrite(OWNER_A, "foo"), wildcardRewrite(OWNER_B)));
        assertThat(index.hasConstructorRewrites()).isFalse();
    }

    @Test
    void hasConstructorRewritesReturnsTrueWhenConstructorRewritePresent() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(constructorRewrite(OWNER_A)));
        assertThat(index.hasConstructorRewrites()).isTrue();
    }

    // --- constructorCandidates ---

    @Test
    void constructorCandidatesReturnsEmptyWhenNoneRegistered() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of());
        assertThat(index.constructorCandidates(OWNER_A)).isEmpty();
    }

    @Test
    void constructorCandidatesReturnsEmptyForNonMatchingOwner() {
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(constructorRewrite(OWNER_A)));
        assertThat(index.constructorCandidates(OWNER_B)).isEmpty();
    }

    @Test
    void constructorCandidatesReturnsRewriteForMatchingOwner() {
        final MethodRewrite ctor = constructorRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(ctor));
        assertThat(index.constructorCandidates(OWNER_A)).containsExactly(ctor);
    }

    @Test
    void constructorCandidatesMultipleRewritesSameOwnerAllReturned() {
        final MethodRewrite first = constructorRewrite(OWNER_A);
        final MethodRewrite second = constructorRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(first, second));
        assertThat(index.constructorCandidates(OWNER_A)).containsExactly(first, second);
    }

    @Test
    void constructorCandidatesDifferentOwnersDontInterfere() {
        final MethodRewrite ctorA = constructorRewrite(OWNER_A);
        final MethodRewrite ctorB = constructorRewrite(OWNER_B);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(ctorA, ctorB));
        assertThat(index.constructorCandidates(OWNER_A)).containsExactly(ctorA);
        assertThat(index.constructorCandidates(OWNER_B)).containsExactly(ctorB);
    }

    // --- constructor rewrite interaction with regular candidates ---

    @Test
    void constructorRewriteAppearsInCandidatesAsWildcard() {
        // Constructor rewrites fall through to the wildcard slot in the regular index
        final MethodRewrite ctor = constructorRewrite(OWNER_A);
        final MethodRewriteIndex index = new MethodRewriteIndex(List.of(ctor));
        assertThat(index.candidates(OWNER_A, ClassFiles.CONSTRUCTOR_METHOD_NAME)).containsExactly(ctor);
        assertThat(index.candidates(OWNER_A, "anyOtherMethod")).containsExactly(ctor);
    }
}
