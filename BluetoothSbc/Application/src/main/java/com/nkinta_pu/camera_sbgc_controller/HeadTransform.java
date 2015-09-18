package com.nkinta_pu.camera_sbgc_controller;

import android.opengl.Matrix;
import android.util.FloatMath;

/**
 * Describes the head transform independently of any eye parameters. 
 */
public class HeadTransform
{
	private static final float GIMBAL_LOCK_EPSILON = 0.01F;
	private static final float PI = 3.141593F;
	private final float[] mHeadView;

	public HeadTransform()
	{
		mHeadView = new float[16];
		Matrix.setIdentityM(mHeadView, 0);
	}

	float[] getHeadView()
	{
		return mHeadView;
	}

	public void getHeadView(float[] headView, int offset)
	{
		if (offset + 16 > headView.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		System.arraycopy(mHeadView, 0, headView, offset, 16);
	}

	public void getTranslation(float[] translation, int offset)
	{
		if (offset + 3 > translation.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		for (int i = 0; i < 3; i++)
			translation[(i + offset)] = mHeadView[(12 + i)];
	}

	public void getForwardVector(float[] forward, int offset)
	{
		if (offset + 3 > forward.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		for (int i = 0; i < 3; i++)
			forward[(i + offset)] = (-mHeadView[(8 + i)]);
	}

	public void getUpVector(float[] up, int offset)
	{
		if (offset + 3 > up.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		for (int i = 0; i < 3; i++)
			up[(i + offset)] = mHeadView[(4 + i)];
	}

	public void getRightVector(float[] right, int offset)
	{
		if (offset + 3 > right.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		for (int i = 0; i < 3; i++)
			right[(i + offset)] = mHeadView[i];
	}

	public void getQuaternion(float[] quaternion, int offset)
	{
		if (offset + 4 > quaternion.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		float[] m = mHeadView;
		float t = m[0] + m[5] + m[10];
		float s, w, x, y, z;
		if (t >= 0.0F) {
			s = FloatMath.sqrt(t + 1.0F);
			w = 0.5F * s;
			s = 0.5F / s;
			x = (m[9] - m[6]) * s;
			y = (m[2] - m[8]) * s;
			z = (m[4] - m[1]) * s;
		}
		else
		{
			if ((m[0] > m[5]) && (m[0] > m[10])) {
				s = FloatMath.sqrt(1.0F + m[0] - m[5] - m[10]);
				x = s * 0.5F;
				s = 0.5F / s;
				y = (m[4] + m[1]) * s;
				z = (m[2] + m[8]) * s;
				w = (m[9] - m[6]) * s;
			}
			else
			{
				if (m[5] > m[10]) {
					s = FloatMath.sqrt(1.0F + m[5] - m[0] - m[10]);
					y = s * 0.5F;
					s = 0.5F / s;
					x = (m[4] + m[1]) * s;
					z = (m[9] + m[6]) * s;
					w = (m[2] - m[8]) * s;
				}
				else {
					s = FloatMath.sqrt(1.0F + m[10] - m[0] - m[5]);
					z = s * 0.5F;
					s = 0.5F / s;
					x = (m[2] + m[8]) * s;
					y = (m[9] + m[6]) * s;
					w = (m[4] - m[1]) * s;
				}
			}
		}
		quaternion[(offset + 0)] = x;
		quaternion[(offset + 1)] = y;
		quaternion[(offset + 2)] = z;
		quaternion[(offset + 3)] = w;
	}

	private float removeLimitByOldAngle(float before, float now) {
		float diff = now - before;
		float multiple = diff / (float)(2.0f* Math.PI);
		int intMultiple = (int)Math.floor(multiple + 0.5);
		float newDiff = diff - intMultiple * (2.0f * (float)Math.PI);
		float newAngle = before + newDiff;

		return newAngle;
	}

	private double getDifferenceAngle(double angle1, double angle2) {

		double a1s;
		double a1c;
		double a2s;
		double a2c;
		double innerProduct;
		double outerProduct;

		a1s = Math.sin(angle1);
		a1c = Math.cos(angle1);

		a2s = Math.sin(angle2);
		a2c = Math.cos(angle2);

		innerProduct = a2s * a1s + a2c * a1c;
		outerProduct = a1s * a2c - a1c * a2s;

		float angle = (float)Math.atan2(outerProduct, innerProduct);

		return angle;
	}

	public void getEulerAngles(float[] beforeEulerAngles, float rollOffset, float[] eulerAngles, int offset)
	{
		if (offset + 3 > eulerAngles.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		// float[] headView = mHeadView;
		float[] rollOffsetMatrix = new float[16];
		Matrix.setIdentityM(rollOffsetMatrix, 0);
		Matrix.rotateM(rollOffsetMatrix, 0, rollOffset, 0, 0, 1);

		float[] headView = new float[16];
		Matrix.multiplyMM(headView, 0, rollOffsetMatrix, 0, mHeadView, 0);

		/*
		Matrix.rotateM(headView, 0, rollOffset, 0, 0, 1);
		*/

		/*
		float yaw, roll, pitch = (float)Math.asin(mHeadView[6]);
		if (FloatMath.sqrt(1.0F - mHeadView[6] * mHeadView[6]) >= 0.01F)
		{
			yaw = (float)Math.atan2(-mHeadView[2], mHeadView[10]);
			roll = (float)Math.atan2(-mHeadView[4], mHeadView[5]);
		}
		else
		{
			yaw = 0.0F;
			roll = (float)Math.atan2(mHeadView[1], mHeadView[0]);
		}
		*/

		float yaw1, pitch1;
		float roll1 = (float)Math.asin(headView[4]);
		if (FloatMath.sqrt(1.0F - headView[4] * headView[4]) >= 0.01F)
		{
			yaw1 = (float)Math.atan2(-headView[8], headView[0]);
			pitch1 = (float)Math.atan2(-headView[6], headView[5]);
		}
		else
		{
			if  (offset + 3 < beforeEulerAngles.length) {
				yaw1 = beforeEulerAngles[(offset + 1)];
			}
			else {
				yaw1 = 0.0F;
			}
			pitch1 = (float)Math.atan2(headView[1], headView[10]) - yaw1;
		}

		float yaw2, pitch2;
		float roll2 = (float)Math.PI - (float)Math.asin(headView[4]);
		if (FloatMath.sqrt(1.0F - headView[4] * headView[4]) >= 0.01F)
		{
			yaw2 = (float)Math.atan2(headView[8], -headView[0]);
			pitch2 = (float)Math.atan2(headView[6], -headView[5]);
		}
		else
		{
			if  (offset + 3 < beforeEulerAngles.length) {
				yaw2 = beforeEulerAngles[(offset + 1)];
			}
			else {
				yaw2 = 0.0F;
			}
			pitch2 = (float)Math.atan2(-headView[1], -headView[10]) - yaw2;
		}

		if  (offset + 3 > beforeEulerAngles.length) {
			eulerAngles[(offset + 0)] = roll1;
			eulerAngles[(offset + 1)] = yaw1;
			eulerAngles[(offset + 2)] = pitch1;
		}
		else {
			float bRoll = beforeEulerAngles[(offset + 0)];
			float bYaw = beforeEulerAngles[(offset + 1)];
			float bPitch = beforeEulerAngles[(offset + 2)];

			float diff1 = 0;
			diff1 += Math.pow(getDifferenceAngle(bPitch, pitch1), 2);
			diff1 += Math.pow(getDifferenceAngle(bYaw, yaw1), 2);
			diff1 += Math.pow(getDifferenceAngle(bRoll, roll1), 2);

			float diff2 = 0;
			diff2 += Math.pow(getDifferenceAngle(bPitch, pitch2), 2);
			diff2 += Math.pow(getDifferenceAngle(bYaw, yaw2), 2);
			diff2 += Math.pow(getDifferenceAngle(bRoll, roll2), 2);

			float newPitch, newYaw, newRoll;
			if (diff1 > diff2) {
				newPitch = pitch2;
				newYaw = yaw2;
				newRoll = roll2;
			} else {
				newPitch = pitch1;
				newYaw = yaw1;
				newRoll = roll1;
			}

			float resultPitch = removeLimitByOldAngle(bPitch, newPitch);
			float resultYaw = removeLimitByOldAngle(bYaw, newYaw);
			float resultRoll = removeLimitByOldAngle(bRoll, newRoll);

			// if (bRoll > resultRoll) {
			// 	throw new IllegalArgumentException("hi");
			// }

			eulerAngles[(offset + 0)] = resultRoll;
			eulerAngles[(offset + 1)] = resultYaw;
			eulerAngles[(offset + 2)] = resultPitch;
		}
	}
}