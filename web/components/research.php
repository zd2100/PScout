<div>
						
	<div class="page-header">
		<h2>Research Paper</h2>
	</div>
	<div class="well well-lg">
	<p><h3>Abstract</h3></p>
	<p>Modern smartphone operating systems (OSs) have been developed
	with a
	greater emphasis on security and protecting privacy. One of the
	mechanisms these systems use to protect users is a permission system,
	which requires developers to declare what sensitive resources their
	applications will use, has users agree with this request when they
	install the application and constrains the application to the requested
	resources during runtime. As these permission systems become more
	common, questions have risen about their design and implementation. In
	this paper, we perform an analysis of the permission system of the
	Android smartphone OS in an attempt to begin answering some of these
	questions. Because the documentation of Android's permission system
	is
	incomplete and because we wanted to be able to analyze several versions
	of Android, we developed PScout, a tool that extracts the permission
	specification from the Android OS source code using static analysis.
	PScout overcomes several challenges, such as scalability due to
	Android's 3.4 million line code base, accounting for permission
	enforcement across processes due to Android's use of IPC, and
	abstracting Android's diverse permission checking mechanisms into a
	single
	primitive for analysis.
	</p>

	<p>
	We use PScout to analyze 4 versions of Android spanning version 2.2 up
	to the recently released Android 4.0. Our main findings are that while
	Android has over 75 permissions, there is little redundancy in the
	permission specification. However, if applications could be
	constrained
	to only use documented APIs, then about 22% of the non-system
	permissions are actually unnecessary. Finally, we find that a
	trade-off
	exists between enabling least-privilege security with fine-grained
	permissions and maintaining stability of the permission specification
	as
	the Android OS evolves.
	</p>
</div>
	<i>Kathy Wain Yee Au, Yi Fan Zhou, Zhen Huang and David Lie.&nbsp;</i>
	<a class="bold-underline" href="http://www.eecg.toronto.edu/%7Elie/papers/PScout-CCS2012-web.pdf">
	PScout: Analyzing the Android Permission Specification</a>
	. In the Proceedings of the 19th ACM Conference on Computer and Communications Security (CCS 2012). October 2012. &nbsp;[<a href="http://www.eecg.toronto.edu/%7Elie/papers/PScout-CCS2012-web.pdf">Full
	Paper</a>] [<a href="PScout-CCS2012-slides.pdf">Presentation Slides</a>]

</div>