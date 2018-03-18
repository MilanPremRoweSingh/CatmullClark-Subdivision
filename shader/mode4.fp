//Milan Singh 260653803
// mode 4 - ambient and Lambertian and Specular with shadow map

uniform sampler2D shadowMap; 
uniform float sigma;

varying vec3 N;  // surface normal in camera 
varying vec3 v;  // surface fragment location in camera 
varying vec4 vL; // surface fragment location in light view NDC
 
void main(void) {

	vec3 L = normalize(gl_LightSource[0].position.xyz - v);
	vec4 Lambertian = max( dot(N,L), 0.0 ) * gl_LightSource[0].diffuse * gl_FrontMaterial.diffuse;
	vec4 Ambient 	= gl_LightSource[0].ambient * gl_FrontMaterial.ambient;
	
	vec4 Specular;
	//if ( Lambertian.x > 0.0 && Lambertian.y > 0.0  && Lambertian.z > 0.0  )
	{
		vec3 norV 		= normalize ( -v );
		vec3 h 			= normalize( ( norV + L ) );
		float cosTerm 	= max( 0.0, dot(h,N) );
			  cosTerm	= pow( cosTerm, gl_FrontMaterial.shininess );
		Specular 		= gl_FrontLightProduct[0].specular * cosTerm;
	}
	
	// TODO: Objective 6: ambient, Labertian, and Specular with shadow map.
	// Note that the shadow map lookup should only modulate the Lambertian and Specular component.
	
	vec4 vLdivW			= vL / vL.w;
	vec4 ShadowCoord 	= ( vLdivW + vec4(1,1,1,1) ) * 0.5; //Transform from [-1,1] to [0,1] for texture map
	float lightViewDepth = texture( shadowMap, ShadowCoord.xy ).z;
	
	float shadowing = ( lightViewDepth + sigma < ShadowCoord.z   ) ?  0.0f : 1.0f;	
	
	
	//gl_FragColor = vec4( lightViewDepth );
	gl_FragColor =  Ambient + ( Lambertian + Specular )*shadowing;
}