using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Test : MonoBehaviour {

	// Use this for initialization
	void Start() {
		StartCoroutine(BeginFix());
	}

	// Update is called once per frame
	void Update() {

	}

	IEnumerator BeginFix()
	{
		while (true)
		{
			try
			{
				if (FixAndroidOCrash())
				{
					Debug.Log( "FixAndroidOCrash Suceecss" );
					yield break;
				}
			}
			catch (Exception ex)
			{
				Debug.Log("FixAndroidOCrash Error ! "+ ex.ToString());
				yield break;
			}
			
			yield return new WaitForSeconds(1);
		}
		
	}

	public bool FixAndroidOCrash()
	{
#if UNITY_ANDROID
		using (AndroidJavaClass jc = new AndroidJavaClass("com.fuckunity.FixAndroidOCrash"))
		{
			return jc.CallStatic<bool>("TryFix", true);
		}
#else
		return false;
#endif
	}
}
