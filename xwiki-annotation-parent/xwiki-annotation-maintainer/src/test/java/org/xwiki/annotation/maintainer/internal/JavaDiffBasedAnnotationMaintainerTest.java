package org.xwiki.annotation.maintainer.internal;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.maintainer.AnnotationState;

/**
 * Test class for Java Diff based annotation Maintainer.
 * 
 * @version $Id$
 */
public class JavaDiffBasedAnnotationMaintainerTest extends JavaDiffBasedAnnotationMaintainer
{
    /**
     * @param state the state of the annotation
     * @param id the id of the annotation
     * @param offset the offset of the annotation
     * @param length the length of the annotation
     * @return an annotation with only the specified fields filled in
     */
    public Annotation getFakeAnnotation(AnnotationState state, int id, int offset, int length)
    {
        return new Annotation(null, null, null, state, null, null, null, id, offset, length);
    }

    /**
     * Deletion after annotation.
     */
    @Test
    public void deletionAfter01()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 14, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Addition after annotation.
     */
    @Test
    public void additionAfter01()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 14, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked deletion after annotation.
     */
    @Test
    public void deletionAfter02()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked addition after annotation.
     */
    @Test
    public void additionAfter02()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn01()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 10, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn01()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn02()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 8, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn02()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn03()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 2);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn03()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 2);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked deletion before annotation.
     */
    @Test
    public void deletionBefore01()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 1);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked addition before annotation.
     */
    @Test
    public void additionBefore01()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 1);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Deletion before annotation.
     */
    @Test
    public void deletionBefore02()
    {
        String previousContent = "This is not a simple sentence.";
        String content = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 5, 3);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Addition before annotation.
     */
    @Test
    public void additionBefore02()
    {
        String content = "This is not a simple sentence.";
        String previousContent = "This is a simple sentence.";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 5, 3);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularDeletion01()
    {
        String previousContent = "block2 block1 block1 block2";
        String content = "block2 block1 block2";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 19, 7);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularDeletion02()
    {
        String previousContent = "another, limping, mimics the cripple who flew.__TEST__The Poet";
        String content = "another, limping, mimics the cripple who flew.__The Poet bears a";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 54, 8);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularAddition01()
    {
        String previousContent = "block2 block1 block2";
        String content = "block2 block1 block1 block2";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 7);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularAddition02()
    {
        String previousContent = "another, limping, mimics the cripple who flew.__The Poet bears a";
        String content = "another, limping, mimics the cripple who flew.__TEST__The Poet bears a";
        Annotation currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 48, 6);
        recomputeProperties(currentAnnotation, previousContent, content);
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }
}
