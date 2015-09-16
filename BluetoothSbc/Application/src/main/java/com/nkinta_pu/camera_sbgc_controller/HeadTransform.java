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

	private float getContinuesAngle(float before, float now) {
		float tempDiff = now - before;
		float diff = 0;
		if (tempDiff > 0) {
			diff = (tempDiff + (float) Math.PI) % (float) (2 * Math.PI) - (float) Math.PI;
		}
		else {
			diff = (-tempDiff + (float) Math.PI) % (float) (2 * Math.PI) - (float) Math.PI;
			diff = -diff;
		}
		diff = (tempDiff + (float) Math.PI) % (float) (2 * Math.PI) - (float) Math.PI;

		return now;
	}

	public void getEulerAngles(float[] beforeEulerAngles, float rollOffset, float[] eulerAngles, int offset)
	{
		if (offset + 3 > eulerAngles.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		float[] headView = mHeadView;
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

		float yaw, pitch, roll = (float)Math.asin(headView[4]);
		if (FloatMath.sqrt(1.0F - headView[4] * headView[4]) >= 0.01F)
		{
			yaw = (float)Math.atan2(-headView[8], headView[0]);
			pitch = (float)Math.atan2(-headView[6], headView[5]);
		}
		else
		{
			yaw = 0.0F;
			pitch = (float)Math.atan2(headView[1], headView[10]);
		}

		if  (offset + 3 > beforeEulerAngles.length) {
			eulerAngles[(offset + 0)] = roll;
			eulerAngles[(offset + 1)] = yaw;
			eulerAngles[(offset + 2)] = pitch;
		}
		else {
			float bPitch = beforeEulerAngles[(offset + 0)];
			float bYaw = beforeEulerAngles[(offset + 1)];
			float bRoll = beforeEulerAngles[(offset + 2)];

			float diff = 0;
			diff += 1 - Math.sin(bPitch) * Math.sin(pitch) - Math.cos(bPitch) * Math.cos(pitch);
			diff += 1 - Math.sin(bYaw) * Math.sin(yaw) - Math.cos(bYaw) * Math.cos(yaw);
			diff += 1 - Math.sin(bRoll) * Math.sin(roll) - Math.cos(bRoll) * Math.cos(roll);


			float newPitch, newYaw, newRoll;
			if (diff > 2.0) {
				newPitch = pitch + (float) Math.PI;
				newYaw = yaw + (float) Math.PI;
				newRoll = -roll;
			} else {
				newPitch = pitch;
				newYaw = yaw;
				newRoll = roll;
			}

			newPitch = getContinuesAngle(bPitch, newPitch);
			newYaw = getContinuesAngle(bYaw, newYaw);
			newRoll = getContinuesAngle(bRoll, newRoll);

			eulerAngles[(offset + 0)] = newRoll;
			eulerAngles[(offset + 1)] = newYaw;
			eulerAngles[(offset + 2)] = newPitch;
		}
	}
}