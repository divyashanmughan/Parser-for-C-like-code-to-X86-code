package compilerParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.text.html.HTMLDocument.Iterator;

import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.TokenParseException;
import edu.cornell.cs.sam.io.Tokenizer;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;

public class BaliX86Compiler{

	public static int labelNo=1;
	public static int localVar = 0;
	public static int h= 0;
	public static String error = "";
	public static int er=0;
	public static int z=-4;
	public static Map<String, Integer> hmg = new HashMap<String, Integer>();
	static String compiler(String fileName) throws TokenParseException, FileNotFoundException, IOException 

	{

		try
		{
			SamTokenizer f = new SamTokenizer (fileName);
			String pgm = getProgram(f);

			return pgm;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return "STOP\n";
		}

	}
	static String getProgram(SamTokenizer f) throws IOException
	{

		String pgm="";

		while(f.peekAtKind()!=TokenType.EOF)
		{

			pgm+= getMethod(f);
			localVar=0;
			h=0;
			z=-4;


		}
		String osCode = "%include "+"\"io.inc\""+"\nsection .text\n"+"global CMAIN\n" ;
		pgm =  osCode+pgm;
		if(er==1)
		{
			return error;
		}
		else
		{
			return pgm;
		}

	}
	static String getMethod(SamTokenizer f)
	{String methodName = null;
	String body =null;

	try
	{
		h = checkInvalidToken(f);
		if(h==1)
		{
			error+="Error : Invalid Token before method\n";
			er=1;
			System.out.println("Error : Invalid Token before method\n");
			System.exit(1);}

		f.match("int");

		try
		{
			methodName = f.getWord(); 
		}
		catch(Exception e)
		{
			er=1;
			error+=" method Name is missing\n ";

			System.out.println("Error :  method Name is missing\n");
			System.exit(1);

		}
	}catch(Exception e)
	{
		try
		{
			methodName = f.getWord(); 
			error+="Error : Return Data Type of Method is not int\n";
			er=1;
			System.out.println("Error :  Return Data Type of Method is not int\n");
			System.exit(1);

		}
		catch(Exception e1)
		{
			error+="Error : No Return Data Type for Method\n";
			er=1;
			System.out.println("Error :  No Return Data Type for Method\n");
			System.exit(1);

		}

	}




	Map<String, Integer> hm = new HashMap<String, Integer>();
	//hm.put("rv", 1);
	if (!(f.check('(')))
	{
		error+="Error : Paranthesis missing after method\n";
		er=1;
		System.out.println("Error :  Paranthesis missing after method\n");
		System.exit(1);

	}
	String r1;
	if (f.check(')'))
	{int formals =0;
	try
	{

		if (formals == hmg.get(methodName))
			;
		else
		{
			System.out.println("Error : Wrong No of arguments passed in method ");
			System.exit(1);
		}
	}
	catch(Exception e)
	{
		hmg.put(methodName, formals);
		//System.out.println(methodName + " " +hmg.get(methodName));
	}

	body = getBody(f,hm,methodName);

	if (methodName.equals("main"))
	{
		r1= "CMAIN" + ":\n " +"mov ebp ,esp ;\n"+"push ebp ;\n"+"mov ebp ,esp ;\n"+ body;
	}
	else
	{	r1= methodName + ":\n "+"push ebp ;\n"+"mov ebp ,esp ;\n"+ body;

	}
	}
	else
	{
		int formals = getFormals(f,hm,1,1);
		int formals1=((formals+1)*4);
		try
		{

			if (formals == hmg.get(methodName))
				;
			else
			{
				System.out.println("Error : Wrong No of arguments passed in method ");
				System.exit(1);
			}
		}
		catch(Exception e)
		{
			hmg.put(methodName, formals);
			//System.out.println(methodName + " " +hmg.get(methodName));
		}
		for (int u=2;u<=(formals+1);u++)
		{
			for ( String key: hm.keySet()) {
				if (u==hm.get(key))
				{
					hm.put(key, formals1) ;

				}

			}
			formals1 = formals1 -4;

		}

		if (!(f.check(')')))
		{

			error+="Error :  Closing paranthesis ) is missing\n";
			er=1;
			System.out.println("Error :  Closing paranthesis ) is missing\n");
			System.exit(1);

		}

		body = getBody(f,hm,methodName);

		if (methodName.equals("main"))
		{
			r1= "CMAIN" + ":\n " +"mov ebp ,esp ;\n"+"push ebp ;\n"+"mov ebp ,esp ;\n"+ body;
		}
		else
		{	r1= methodName + ":\n "+"push ebp ;\n"+"mov ebp ,esp ;\n"+ body;

		}
	}
	return r1;




	}
	static String[] getExp(SamTokenizer f,Map<String, Integer> hm) 
	{
		
		String[] expression =new String[2];
		expression[0]="";
		expression[1]="";
		switch (f.peekAtKind()) {

		case INTEGER: //E -> integer
		{	
			expression[0] = "mov dword eax , " + f.getInt() + ";\n" ;
			expression[1]="4";
			return expression;
			
		}

		case OPERATOR:  // Checking for E -> '('E+E')','('E-E')','('E/E')'
		{
			if(f.check('-'))
			{
				switch(f.peekAtKind())
				{
				case INTEGER: 
				{
					expression[0] = "mov dword eax , "+"-"+f.getInt()+"\n";

					break;
				}
				case WORD:
				{
					String[] a2 = new String[2];
					 a2= (getExp(f,hm));
					String a3="neg eax\n";
					expression[0]=a2[0]+a3;
					expression[1]=a2[1];
				}
				}
			}
			else if (f.check('('))
			{

				if(f.check('-'))
				{
					String[] a2 = new String[2];
					 a2= (getExp(f,hm));
					String a3="neg eax;\n";
					expression[0]=a2[0]+a3;
					expression[1]=a2[1];
					if (!(f.check(')')))

					{

						error +="Error : Missing ) at end of expression\n";
						er=1;
						System.out.println("Error :  Missing ) at end of expression\n");
						System.exit(1);
					}
				}
				else if (f.check('!'))
				{
					String[] a2 = new String[2];
					 a2= (getExp(f,hm));
					String a3="not eax;\n";
					expression[0]=a2[0]+a3;
					expression[1]="4";
					if (!(f.check(')')))

					{
						error +="Error : Missing ) at end of expression\n";
						er=1;
						System.out.println("Error :  Missing ) at end of expression\n");
						System.exit(1);
					}
				}

				else
				{
					String[] s12 = new String[2];
					 s12= (getExp(f,hm));
					 String s1=s12[0];
					if (f.check(')'))
					{expression[0]= s1 ;
					expression[1]=s12[1];}
					else
					{
						String s2 ="";


						switch(f.getOp())  {
						case '+':  s2 = "add";
						break;
						case '-':  s2 = "sub";
						break;
						case '*':  s2 = "imul";
						break;
						case '/':  s2 = "idiv";
						break;
						case '&':  s2 = "and";
						break;
						case '|':  s2 = "or";
						break;
						case '<':  s2 = "less";
						break;
						case '>':  s2 = "greater";
						break;
						case '=': 
						{s2 = "equal";

						//f.getOp();

						}
						break;
						default:
						{
							error +="Error : Invalid Operation\n";
							er=1;
							System.out.println("Error :  Invalid Operation\n");
							System.exit(1);
						}
						break;
						}
						if(f.check(')'))
						{

							error +="Error : Expression is missing after operator\n";
							er=1;
							System.out.println("Error : Expression is missing after operator\n");
							System.exit(1);
						}
						else
						{ String s3 = (getExp(f,hm))[0];
						if((s2.equals("greater")))
						{
							String s4 = s1+""+"push eax;\n"+ s3+""+"pop ebx"+ ";\n";
							expression[0] =s4;
							expression[1]="1";
						}
						else if((s2.equals("less")))
						{
							String s4 = s1+""+"push eax;\n"+ s3+""+"pop ebx"+ ";\n";
							expression[0] =s4;
							expression[1]="2";
						 
						}
						else if((s2.equals("equal")))
						{
							String s4 = s1+""+"push eax;\n"+ s3+""+"pop ebx"+ ";\n";
							expression[0] =s4;
							expression[1]="3";
						}
						else if((s2.equals("idiv")))
						{
							String s4 = s3+""+"push eax;\n"+ s1+""+"pop ebx"+ ";\nmov edx ,0;\n"+"idiv ebx;\n";
							expression[0] =s4;
						}

						else
						{
							String s4 = s1+""+"push eax;\n"+ s3+""+"pop ebx"+ ";\n"+s2+ " ebx , eax;\n"+"mov eax ,ebx;\n";
							expression[0] =s4;
						}

						if (!(f.check(')')))

						{
							//System.out.println("Error : Missing ) at end of expression");
							error +="Error : Missing ) at end of expression\n";
							er=1;
							System.out.println("Error : Missing ) at end of expression\n");
							System.exit(1);
						}


						}
					}
					//	}
				}
			}
			else
			{


				error +="Error : Invalid Statement\n";
				er=1;
				System.out.println("Error : Invalid Statement\n");
				System.exit(1);
			}
			//	System.out.println(expression);
			return expression;
		}
		case WORD:
		{
			String variable = f.getWord();
			int h=0;
			if(variable.equals("true"))
				{expression[0]= "mov dword eax , 4294967295 ;\n" ;
				expression[1]="4";
			return expression;}
			else if(variable.equals("false"))
			{
				expression[0]="mov dword eax , 0;\n";
				expression[1]="4";
				return expression;
			}
			else if(f.check('('))
			{

				if (f.check(')'))
				{
					try
					{

						if (h == hmg.get(variable))
							;
						else
						{
							System.out.println("Error : Wrong No of arguments passed in method ");
							System.exit(1);
						}
					}
					catch(Exception e)
					{
						hmg.put(variable, h);
						//System.out.println(variable + " " +hmg.get(variable));
					}
					expression[0]=	expression[0]+" CALL " + variable + "\n"  + "add esp , " + Integer.toString(4*h)+"\n";
					return expression;
				}
				expression[0]+=(getExp(f,hm))[0]+" push eax;\n";
				int g=1;

				h=h+1;
				while(g==1)
				{
					if(f.check(')'))
					{
						g=2;
					}
					else if(f.check(',')) 
					{
						expression[0]+=(getExp(f,hm))[0]+" push eax;\n";
						h=h+1;
					}
					else
					{
						error += "Missing ) ; in method calling\n";
						er=1;
						g=2;
						System.out.println("Missing ) ; in method calling\n");
						System.exit(1);
					}

				}
				try
				{
					//System.out.println(h);
					if (h == hmg.get(variable))
						;
					else
					{
						System.out.println("Error : Wrong No of arguments passed in method ");
						System.exit(1);
					}
				}
				catch(Exception e)
				{
					hmg.put(variable, h);
					//System.out.println(variable + " " +hmg.get(variable));
				}
				expression[0]=	expression[0]+" CALL " + variable + "\n"  + "add esp , " + Integer.toString(4*h)+"\n";
return expression;
			}
			else
			{
				int var8=0;
				try
				{
					var8 = hm.get(variable);
				}
				catch(Exception e)
				{
					System.out.println("Variable not initialised");
					System.exit(1);
				}
				if (var8<0)
				{
					var8=(var8*(-1));
					expression[0] = "mov dword  eax , [ebp - " + Integer.toString(var8)+"];\n";
				}
				else
				{
					expression[0] = "mov dword eax , [ebp + " + Integer.toString(var8)+"];\n";
				}
			}
			return expression;
		}
		default:   return expression;
		}
	}

	static int getFormals(SamTokenizer f, Map<String, Integer> hm,int a,int b){     //GENERATING SAM CODE FOR FORMAL PARAMETERS
		switch(f.peekAtKind())
		{

		case WORD :
		{

			try
			{
				f.match("int");
				try
				{

					String variableName = f.getWord();
					hm.put(variableName, a+1);
				}
				catch(Exception e)
				{

					error +="Error : Value Of Formals is not word\n";
					er=1;
					System.out.println("Error : Value Of Formals is not word\n");
					System.exit(1);
					switch(f.peekAtKind())
					{
					case INTEGER :f.getInt();

					}
				}

			}
			catch(Exception e)
			{
				try
				{
					f.getWord();
					error +=" Error : Data type of variable is not int\n";
					er=1;
					System.out.println("Error : Data type of variable is not int\n");
					System.exit(1);
				}
				catch(Exception e1)
				{

					error +=" Error :  Data type in Formals is missing\n";
					er=1;
					System.out.println("Error : Data type in Formals is missing\n");
					System.exit(1);
				}
			}


			if (f.check(','))
			{

				return b+getFormals(f,hm,a+1,b);
			}

			return b;

		}
		default:
		{
			System.out.println("Error : Invalid Token in formals");
			System.exit(1);
			return 0;
		}
		}



	}
	static String getBody(SamTokenizer f,Map<String, Integer> hm,String methodName)
	{
		int l5=0;
		if (!(f.check('{')))
		{
			error+="Error :   Opening paranthesis { for method is missing\n";
			er=1;
			System.out.println("Error :  Opening paranthesis { for method is missing\n");
			System.exit(1);
			while(f.peekAtKind() != Tokenizer.TokenType.WORD)
			{
				switch(f.peekAtKind())
				{
				case INTEGER :  {
					int invalidInt = f.getInt();
					error+= invalidInt+" Invalid Token\n ";
					er=1;
					System.out.println("Error : Invalid Token\n"+ " " +invalidInt);
					System.exit(1);
					break;
				}
				case OPERATOR : 
				{
					int invaliOp = f.getOp();
					error+=invaliOp +" Invalid Token\n ";
					er=1;
					System.out.println("Error : Invalid Token\n"+ " "+invaliOp);
					System.exit(1);
					break;
				}
				}
			}

		}

		String declare="";

		int w=1;
		while(w==1)
		{
			if(f.check("int"))
			{
				declare+=getDeclarations(f,hm);}

			else
				w=2;
		}
		int w1=1;
		while(w1==1)
		{
			if (f.check('}'))
			{
				if(methodName.equals("main"))
				{
					declare+= methodName+"End:\n " +"add esp , "+(localVar*4)+";\n"+"PRINT_DEC  4 , eax"+";\nNEWLINE\n"+"pop ebp ;\n "+"ret;\n";
				}
				else
				{
					declare+= methodName+"End:\n " +"add esp , "+(localVar*4)+";\n"+"pop ebp ;\n "+"ret;\n";	
				}
				w1=2;
			}
			else
			{

				declare+=getStatements(f,hm,methodName,l5);

			}
		}


		return declare;

	}
	static String getDeclarations(SamTokenizer f,Map<String, Integer> hm)
	{
		//int z=2;
		String b= "";
		String variableName= "";

		try
		{
			variableName = f.getWord();
		}
		catch(Exception e)
		{
			System.out.println("Error : Invalid declaration Statement");
			System.exit(1);
		}



		hm.put(variableName, z);       //ADDING VALUES TO SYMBOL TABLE
		z=z-4;
		localVar = localVar+1;
		if(f.check('='))
		{
			b+= (getExp(f,hm))[0] + "push eax;\n";
		}
		else
		{
			b+="add esp ,-4"+"\n";

		}
		int a=1;
		while(a==1)
		{ 
			if(f.check(';'))
			{
				a=2;
			}
			else if (f.check(','))
			{

				try
				{
					variableName = f.getWord();
				}
				catch(Exception e)
				{
					System.out.println("Error : Missing ; in declaration statement or Invalid Token in declaration Statement");
					System.exit(1);
				}

				hm.put(variableName, z);
				z=z-4;
				localVar = localVar+1;
				if(f.check('='))
				{
					b+=(getExp(f,hm))[0]+ "push eax;\n";
				}
				else
				{
					b+="add esp ,-4"+"\n";
				}

			}
			else
			{
				if(f.peekAtKind() != Tokenizer.TokenType.WORD)
				{
					switch(f.peekAtKind())
					{
					case INTEGER :  {
						error+= f.getInt()+" Invalid Token\n ";
						er=1;
						System.out.println("Error : Invalid Token\n");
						System.exit(1);
						break;
					}
					case OPERATOR : 
					{
						error+=f.getOp() +" Invalid Token\n ";
						er=1;
						System.out.println("Error : Invalid Token\n");
						System.exit(1);
						break;
					}
					}
				}
				else
				{
					error+="Error : Missing  ; in declaration statement OR Missing } at end of Method\n";
					er=1;
					System.out.println("Error : Missing  ; in declaration statement OR Missing } at end of Method\n");
					System.exit(1);
					a=2;
				}
			}

		}


		return b;

	}

	static String getStatements(SamTokenizer f,Map<String, Integer> hm,String methodName,int l5) //cHECKING FOR STATEMENTS IN PRODUCTIONS
	{

		String statement ="";
		String variable="";

		switch (f.peekAtKind()) {
		case WORD:
		{

			variable = f.getWord();

			if(variable.equals( "return"))           // CHECKING FOR return statement
			{

				String a1 = "jmp "+ methodName+"End" +"\n";
				String a2 = (getExp(f,hm))[0];
				if (!f.check(';'))

				{

					error +="Error : Missing ; at end of return statement\n";
					er=1;
					System.out.println("Error : Missing  ;at end of return statement\n");
					System.exit(1);
				}
				statement = a2+a1;



			}
			else if (variable.equals("if"))             // CHECKING FOR IF ELSE statement
			{
               String[] a11=new String[2];
               
				if (!(f.check('(')))

				{

					error +="Error : Missing ( in IF statement\n";
					er=1;
					System.out.println("Error : Missing ( in IF statement\n");
					System.exit(1);
				}
				a11=(getExp(f,hm));
				 
				String  a1=a11[0];
                 if (a11[1].equals("1"))
                 {
                	 a1=a1+"cmp ebx , eax;\n"+"jg "+ " ";
                 }
                 else if (a11[1].equals("2"))
                 {
              
                	 a1=a1+"cmp ebx , eax;\n"+"jl "+ " ";
                 }
                 else  if (a11[1].equals("3"))
                 {
                	 a1=a1+"cmp ebx , eax;\n"+"je "+ " ";
                 }
                 else  if (a11[1].equals("4"))
                 {
                	 a1=a1+"mov dword ebx , 0;\n  cmp eax , ebx ;\n jne "+ " ";
                 }
				if (!(f.check(')')))

				{

					error +="Error : Missing ) in IF statement\n";
					er=1;
					System.out.println("Error : Missing ) in IF statement\n");
					System.exit(1);
				}
				int l1= labelNo;
				labelNo=labelNo+1;
				int l2= labelNo;
				labelNo=labelNo+1;
				String a2="label"+Integer.toString(l1)+"\n";
				String a3 = getStatements(f,hm,methodName,l5)+"jmp "+ "label"+Integer.toString(l2)+";\n"+ "label"+Integer.toString(l2)+":\n ";

				if (!(f.check("else")))

				{

					error +="Error : Missing else in IF statement\n";
					er=1;
					System.out.println("Error : Missing else in IF statement\n");
					System.exit(1);

				}
				/*else
				{
					f.getWord();
				}*/
				String a4 = getStatements(f,hm,methodName,l5)+"jmp "+ "label"+Integer.toString(l2)+";\n";
				String a5 = "\n"+"label"+Integer.toString(l1);
				statement = a1+a2+a4+"\n"+a5+":\n"+a3;


			}
			else if (variable.equals("while"))            //CHECKING FOR WHILE STATEMENT
			{
				int l3=labelNo;
				labelNo=labelNo+1;
				int l4=labelNo;
				labelNo=labelNo+1;
				l5=labelNo;
				labelNo=labelNo+1;
				if (!f.check('('))

				{

					error +="Error : Missing ( after while\n ";
					er=1;
					System.out.println("Error :Missing ( after while\n");
					System.exit(1);

				}
                String[] s11 =new String[2];
				String s1 = "label"+Integer.toString(l3)+": ";
				s11=(getExp(f,hm));
				String  s2=s11[0];
                 if (s11[1].equals("1"))
                 {
                	 s2=s2+"cmp ebx , eax;\n"+"jg "+ " ";
                 }
                 else if (s11[1].equals("2"))
                 {
                	 s2=s2+"cmp ebx , eax;\n"+"jl "+ " ";
                 }
                 else  if (s11[1].equals("3"))
                 {
                	 s2=s2+"cmp ebx , eax;\n"+"je "+ " ";
                 }
                 else  if (s11[1].equals("4"))
                 {
                	 s2=s2+"mov dword ebx , 0;\n  cmp eax , ebx ;\n jne "+ " ";
                 }
				if (!f.check(')'))

				{

					error +="Error : Missing ) in while statement\n ";
					er=1;
					System.out.println("Error : Missing ) in while statement\n ");
					System.exit(1);
				}


				String s3 = "label"+Integer.toString(l4);
				String s4 = "jmp "+"label"+Integer.toString(l5);
				String s5 ="label"+Integer.toString(l4)+":\n ";

				String s7 = "jmp "+"label"+Integer.toString(l3);
				String s8 ="label"+Integer.toString(l5)+":\n ";
				String s6 = getStatements(f,hm,methodName,l5);
				statement=s1+s2+s3+";\n"+s4+";\n"+s5+s6+s7+";\n"+s8;


			}
			else if (variable.equals("break"))
			{
				statement =  "jmp "+"label"+Integer.toString(l5)+";\n";

				if (!f.check(';'))

				{

					error +="Error : Missing ; at end of break statement\n";
					er=1;
					System.out.println("Error : Missing ; at end of break statement\n ");
					System.exit(1);
				}

			}
			else

			{

				try
				{
					f.match('=');
				}
				catch(Exception e){
					System.out.println("Error : Invalid Statement");
					System.exit(1);
				}

				String a1 =(getExp(f,hm))[0];

				int y1=1;
				while(y1==1)
				{

					if (f.check(';'))

					{

						y1=2;
					}
					else if (f.peekAtKind() != Tokenizer.TokenType.WORD)
					{
						switch(f.peekAtKind())
						{
						case INTEGER :  {
							error+= f.getInt()+" Invalid Token\n ";
							er=1;
							System.out.println("Error :  Invalid Token Or Missing ; in assignment Statement\n ");
							System.exit(1);
							break;
						}
						case OPERATOR : 
						{
							error+=f.getOp() +" Invalid Token\n ";
							er=1;
							System.out.println("Error :  Invalid Token Or Missing ; in assignment Statement\n ");
							System.exit(1);
							break;
						}
						}
					}
					else
					{
						error+= "Missing ; at end of assignment statement ";
						er=1;
						System.out.println("Error :Missing ; at end of assignment statement  ");
						System.exit(1);
						y1=2;
					}


				}
				int var9=0;
				try
				{
					var9 = hm.get(variable);
				}
				catch(Exception e)
				{
					System.out.println("Variable not initialised");
					System.exit(1);
				}
				statement = a1+ "mov dword [ebp - " + var9+"] ,eax;\n";
				if (var9<0)
				{
					var9=(var9*(-1));
					statement = a1+ "mov dword [ebp - " + var9+"] ,eax;\n";
				}
				else
				{
					statement = a1+ "mov dword [ebp + " + var9+"] ,eax;\n";
				}

			}





			break;
		}

		case OPERATOR:
		{
			if(f.check('{'))	
			{
				int r1=1;
				while(r1==1)
				{
					if(f.check('}'))
					{
						r1=2;
					}
					else
					{
						statement+=getStatements(f,hm,methodName,l5);
					}


				}
			}
			else if (f.check(';'))
			{
				statement = "";
			}
			else
			{
				System.out.println("Error : Invalid Token");
				System.exit(1);
			}

			break;
		}
		default : {
			//	System.out.println("Error : Missing }\n ");

		}


		}


		return statement;

	}
	static int checkInvalidToken(SamTokenizer f)
	{
		switch(f.peekAtKind())
		{
		case INTEGER :
		{
			f.getInt();
			return 1; 
		}
		case OPERATOR :
		{
			f.getOp();
			return 1; 
		}
		default :
		{
			return 0;
		}

		}

	}
	public static void main(String[] args) throws TokenParseException, FileNotFoundException, IOException
	{
		String inputFile = args[0];
		String outputFile = args[1];
		String output1 = compiler(args[0]);
		if (er==1)
		{
			System.out.println(output1);
			System.exit(1);
		}
		else
		{


			File file = new File(outputFile);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(output1);
			bw.close();}

			//System.out.println(compiler("D:\\tests\\good.break.bali"));
	}

}
