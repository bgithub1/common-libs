package com.billybyte.commonlibs.testcases;


import junit.framework.TestCase;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.CorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.NormalizedRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.VectorialCovariance;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

public class CorrelatedRandomVectorGeneratorTest
extends TestCase {

    public CorrelatedRandomVectorGeneratorTest(String name) {
        super(name);
        mean       = null;
        covariance = null;
        generator  = null;
    }

    public void testRank() {
        assertEquals(2, generator.getRank());
    }

    public void testMath226()
        throws MatrixDimensionMismatchException, NonPositiveDefiniteMatrixException {
//        double[] mean = { 1, 1, 10, 1 };
        double[] mean = { 1,1,1,1 };
        double[][] cov = {
                { 1, 3, 2, 6 },
                { 3, 13, 16, 2 },
                { 2, 16, 38, -1 },
                { 6, 2, -1, 197 }
        };
        RealMatrix covRM = MatrixUtils.createRealMatrix(cov);
        JDKRandomGenerator jg = new JDKRandomGenerator();
        jg.setSeed(5322145245211l);
        NormalizedRandomGenerator rg = new GaussianRandomGenerator(jg);
        CorrelatedRandomVectorGenerator sg =
            new CorrelatedRandomVectorGenerator(mean, covRM, 0.0000001, rg);
        Mean m = new Mean();
        double[] avgs = new double[100000];
        
        for (int i = 0; i < 100000; i++) {
            double[] generated = sg.nextVector();
            avgs[i] = m.evaluate(generated);
        }
        double meanOfMeans = m.evaluate(avgs);
        System.out.println(" mean of means: "+meanOfMeans);
        for (int i = 0; i < 10; i++) {
            double[] generated = sg.nextVector();
            assertTrue(Math.abs(generated[0] - 1) > 0.1);
        }

    }

    public void testRootMatrix() {
        RealMatrix b = generator.getRootMatrix();
        RealMatrix bbt = b.multiply(b.transpose());
        for (int i = 0; i < covariance.getRowDimension(); ++i) {
            for (int j = 0; j < covariance.getColumnDimension(); ++j) {
                assertEquals(covariance.getEntry(i, j), bbt.getEntry(i, j), 1.0e-12);
            }
        }
    }

    public void testMeanAndCovariance() throws MatrixDimensionMismatchException {

        VectorialMean meanStat = new VectorialMean(mean.length);
        VectorialCovariance covStat = new VectorialCovariance(mean.length, true);
        for (int i = 0; i < 5000; ++i) {
            double[] v = generator.nextVector();
            meanStat.increment(v);
            covStat.increment(v);
        }

        double[] estimatedMean = meanStat.getResult();
        RealMatrix estimatedCovariance = covStat.getResult();
        for (int i = 0; i < estimatedMean.length; ++i) {
            assertEquals(mean[i], estimatedMean[i], 0.07);
            for (int j = 0; j <= i; ++j) {
                assertEquals(covariance.getEntry(i, j),
                        estimatedCovariance.getEntry(i, j),
                        0.1 * (1.0 + Math.abs(mean[i])) * (1.0 + Math.abs(mean[j])));
            }
        }

    }

    @Override
    public void setUp() {
        try {
            mean = new double[] { 0.0, 1.0, -3.0, 2.3};

            RealMatrix b = MatrixUtils.createRealMatrix(4, 3);
            int counter = 0;
            for (int i = 0; i < b.getRowDimension(); ++i) {
                for (int j = 0; j < b.getColumnDimension(); ++j) {
                    b.setEntry(i, j, 1.0 + 0.1 * ++counter);
                }
            }
            RealMatrix bbt = b.multiply(b.transpose());
            covariance = MatrixUtils.createRealMatrix(mean.length, mean.length);
            for (int i = 0; i < covariance.getRowDimension(); ++i) {
                covariance.setEntry(i, i, bbt.getEntry(i, i));
                for (int j = 0; j < covariance.getColumnDimension(); ++j) {
                    double s = bbt.getEntry(i, j);
                    covariance.setEntry(i, j, s);
                    covariance.setEntry(j, i, s);
                }
            }

            RandomGenerator rg = new JDKRandomGenerator();
            rg.setSeed(17399225432l);
            GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(rg);
            generator = new CorrelatedRandomVectorGenerator(mean,
                                                            covariance,
                                                            1.0e-12 * covariance.getNorm(),
                                                            rawGenerator);
        } catch (DimensionMismatchException e) {
            fail(e.getMessage());
        } catch (NonPositiveDefiniteMatrixException e) {
            fail("not positive definite matrix");
        }
    }

    @Override
    public void tearDown() {
        mean       = null;
        covariance = null;
        generator  = null;
    }

    private double[] mean;
    private RealMatrix covariance;
    private CorrelatedRandomVectorGenerator generator;

}

